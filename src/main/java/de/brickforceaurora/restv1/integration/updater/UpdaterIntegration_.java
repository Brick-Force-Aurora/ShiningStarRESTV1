package de.brickforceaurora.restv1.integration.updater;

import java.io.IOException;
import java.util.UUID;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.config.common.ProductInfo;
import de.brickforceaurora.restv1.integration.api.webv2.IWebContext;
import de.brickforceaurora.restv1.integration.api.webv2.WebMethod;
import de.brickforceaurora.restv1.integration.api.webv2.WebSignal;
import de.brickforceaurora.restv1.integration.updater.http.InstructionsContentType;
import de.brickforceaurora.restv1.integration.util.web.HttpError;
import de.brickforceaurora.restv1.integration.util.web.HttpResp;
import de.brickforceaurora.restv1.util.ParseUtil;
import io.netty.channel.Channel;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.JsonType;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.signal.ISignalHandler;
import me.lauriichan.snowframe.signal.SignalContext;
import me.lauriichan.snowframe.signal.SignalHandler;
import me.lauriichan.snowframe.util.Version;
import me.lauriichan.snowframe.util.http.HttpCode;
import me.lauriichan.snowframe.util.http.HttpData;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.data.HttpEncoded;
import me.lauriichan.snowframe.util.http.data.MultiFormData;
import me.lauriichan.snowframe.util.http.netty.HttpPath;
import me.lauriichan.snowframe.util.http.server.Response;
import me.lauriichan.snowframe.util.http.type.HttpContentType;
import me.lauriichan.snowframe.util.instance.Shared;

@Shared
@Extension
public class UpdaterIntegration_ implements ISignalHandler {

    private static final HttpContentType<byte[]> ZIP_TYPE = HttpContentType.BINARY.restrict("application/zip");

    private final UpdateStorage storage;
    private final ISimpleLogger logger;

    public UpdaterIntegration_(SnowFrame<RestApp> frame) {
        this.storage = new UpdateStorage(frame);
        this.logger = frame.logger();
    }

    public UpdateStorage storage() {
        return storage;
    }

    @SignalHandler
    public void onStart(SignalContext<WebSignal.Start> context) {
        IWebContext<IJson<?>> productJson = context.signal().provider().createContext("updater/v1/product", HttpContentType.JSON);
        productJson.route("list", this::listProduct, WebMethod.get(false));
        productJson.route("id", this::idProduct, WebMethod.get(false));

        IWebContext<IJson<?>> updateJson = context.signal().provider().createContext("updater/v1/update", HttpContentType.JSON);
        updateJson.route("latest", this::latestUpdate, WebMethod.get(false));
        updateJson.route("list", this::listUpdate, WebMethod.get(false));
        updateJson.route("bundle", this::bundleUpdate, WebMethod.get(false));

        IWebContext<MultiFormData> updateForm = context.signal().provider().createContext("updater/v1/update",
            HttpContentType.multiFormData(HttpContentType.JSON, InstructionsContentType.UPDATE_INSTRUCTIONS, ZIP_TYPE));
        updateForm.route("publish", this::publishUpdate, WebMethod.post(true));
    }

    public Response listProduct(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path) {
        int page = Math.max(query.getInt("page", 0), 0);
        JsonObject object = new JsonObject();
        object.put("page", page);
        object.put("max_page", Math.max(Math.ceilDiv(storage.productCount(), 20) - 1, 0));
        JsonArray array = new JsonArray();
        storage.productStream().skip(page * 20).limit(20).forEachOrdered(product -> {
            JsonObject obj = new JsonObject();
            obj.put("id", product.idShort());
            obj.put("name", product.name());
            array.add(obj);
        });
        object.put("products", array);
        return HttpResp.ok("data", object);
    }

    public Response idProduct(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path) {
        String productName = query.getString("product_name");
        if (productName == null || productName.isBlank()) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Query parameter 'product_name' is missing");
        }
        ProductInfo info = storage.productByName(productName);
        if (info == null) {
            return HttpError.notFound("Unknown product '%s'".formatted(productName));
        }
        JsonObject object = new JsonObject();
        object.put("id", info.idShort());
        object.put("name", info.name());
        return HttpResp.ok("info", object);
    }

    public Response latestUpdate(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path) {
        ProductStorage storage;
        try {
            UUID productId = ParseUtil.uuidFromString(query.getString("product_id"));
            storage = this.storage.ofProduct(productId);
            if (storage == null) {
                return HttpError.notFound("Unknown product id '%s'".formatted(productId.toString()));
            }
        } catch (NullPointerException _) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Query parameter 'product_id' is missing");
        } catch (IllegalArgumentException iae) {
            return HttpError.badRequest(HttpError.IO_ERROR, HttpError.IO_ERROR_NAME,
                "Couldn't parse 'product_id' version query parameter; " + iae.getMessage());
        }

        Version version = storage.latest();
        if (version == null) {
            return Response.of(HttpCode.NO_CONTENT);
        }
        return HttpResp.ok("latest_version", IJson.of(version.toString()));
    }

    public Response listUpdate(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path) {
        ProductStorage storage;
        try {
            UUID productId = ParseUtil.uuidFromString(query.getString("product_id"));
            storage = this.storage.ofProduct(productId);
            if (storage == null) {
                return HttpError.notFound("Unknown product id '%s'".formatted(productId.toString()));
            }
        } catch (NullPointerException _) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Query parameter 'product_id' is missing");
        } catch (IllegalArgumentException iae) {
            return HttpError.badRequest(HttpError.IO_ERROR, HttpError.IO_ERROR_NAME,
                "Couldn't parse 'product_id' version query parameter; " + iae.getMessage());
        }

        int page = Math.max(query.getInt("page", 0), 0);
        JsonObject object = new JsonObject();
        object.put("page", page);
        object.put("max_page", Math.max(Math.ceilDiv(storage.versionCount(), 20) - 1, 0));
        JsonArray array = new JsonArray();
        storage.versionStream().skip(page * 20).limit(20).forEachOrdered(version -> array.add(IJson.of(version.toString())));
        object.put("versions", array);
        return HttpResp.ok("data", object);
    }

    public Response bundleUpdate(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path) {
        ProductStorage storage;
        try {
            UUID productId = ParseUtil.uuidFromString(query.getString("product_id"));
            storage = this.storage.ofProduct(productId);
            if (storage == null) {
                return HttpError.notFound("Unknown product id '%s'".formatted(productId.toString()));
            }
        } catch (NullPointerException _) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Query parameter 'product_id' is missing");
        } catch (IllegalArgumentException iae) {
            return HttpError.badRequest(HttpError.IO_ERROR, HttpError.IO_ERROR_NAME,
                "Couldn't parse 'product_id' version query parameter; " + iae.getMessage());
        }

        Version current, target;
        try {
            current = Version.parse(query.getString("current"));
        } catch (RuntimeException iae) {
            return HttpError.badRequest(HttpError.IO_ERROR, HttpError.IO_ERROR_NAME,
                "Couldn't parse 'current' version query parameter; " + iae.getMessage());
        }
        try {
            target = Version.parse(query.getString("target"));
        } catch (RuntimeException iae) {
            return HttpError.badRequest(HttpError.IO_ERROR, HttpError.IO_ERROR_NAME,
                "Couldn't parse 'target' version query parameter; " + iae.getMessage());
        }
        if (!target.isHigher(current)) {
            return HttpError.generic(HttpCode.CONFLICT, HttpError.VERSION_MISMATCH, HttpError.VERSION_MISMATCH_NAME,
                "Target version can not be lower or equal to the current version.");
        }
        MultiFormData updateData;
        try {
            updateData = storage.buildUpdateMultiFormData(current, target);
        } catch (IOException e) {
            logger.error("Failed to bundle update", e);
            return HttpError.serverError(HttpError.IO_ERROR, HttpError.IO_ERROR_NAME, "Failed to retrieve update; " + e.getMessage());
        }
        return Response.of(HttpCode.OK).content(HttpContentType.DEFAULT_MULTIFORM_DATA, updateData);
    }

    public Response publishUpdate(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path,
        HttpData<MultiFormData> body) {
        ProductStorage storage;
        try {
            UUID productId = ParseUtil.uuidFromString(query.getString("product_id"));
            storage = this.storage.ofProduct(productId);
            if (storage == null) {
                return HttpError.notFound("Unknown product id '%s'".formatted(productId.toString()));
            }
        } catch (NullPointerException _) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Query parameter 'product_id' is missing");
        } catch (IllegalArgumentException iae) {
            return HttpError.badRequest(HttpError.IO_ERROR, HttpError.IO_ERROR_NAME,
                "Couldn't parse 'product_id' version query parameter; " + iae.getMessage());
        }

        if (body.isError()) {
            logger.warning("Failed to publish update", body.error());
            return HttpError.badRequest(body);
        }
        MultiFormData data = body.value();
        IJson<?> infoRaw = data.get("info", HttpContentType.JSON);
        if (infoRaw == null || !infoRaw.isObject()) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Info data is missing.");
        }
        JsonObject info = infoRaw.asJsonObject();
        if (!info.has("version", JsonType.STRING)) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Info is missing 'version' field.");
        }
        Version version;
        try {
            version = Version.parse(info.getAsString("version"));
        } catch (IllegalArgumentException iae) {
            return HttpError.badRequest(HttpError.IO_ERROR, HttpError.IO_ERROR_NAME,
                "Couldn't parse 'version' version query parameter, " + iae.getMessage());
        }
        boolean allowsOverwrite = info.getAsBoolean("overwrite", true);
        UpdateInstructions instructions = data.get("instructions", InstructionsContentType.UPDATE_INSTRUCTIONS);
        if (instructions == null) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Instructions data is missing.");
        }
        byte[] zipBytes = data.get("update_zip", ZIP_TYPE);
        if (zipBytes == null) {
            return HttpError.badRequest(HttpError.MISSING_DATA, HttpError.MISSING_DATA_NAME, "Update zip file is missing.");
        }
        try {
            if (storage.publishUpdate(version, instructions, zipBytes, allowsOverwrite)) {
                if (!allowsOverwrite) {
                    return HttpResp.unmodified("Didn't overwrite, as overwrites were not allowed by request.");
                }
                return HttpResp.modified();
            }
            return HttpResp.created();
        } catch (IOException e) {
            logger.error("Failed to publish update", e);
            return HttpError.serverError("Failed to publish update '%s'".formatted(version), e);
        }
    }

}
