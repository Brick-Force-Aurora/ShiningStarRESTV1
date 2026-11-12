package de.brickforceaurora.restv1.integration.util.web;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.snowframe.util.http.HttpCode;
import me.lauriichan.snowframe.util.http.server.Response;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public final class HttpResp {

    public static final int ERROR = -1;
    public static final String ERROR_NAME = "ERROR";

    public static final int OK = 0;
    public static final String OK_NAME = "OK";

    public static final int CREATED = 1;
    public static final String CREATED_NAME = "CREATED";

    public static final int MODIFIED = 2;
    public static final String MODIFIED_NAME = "MODIFIED";

    public static final int NOT_MODIFIED = 3;
    public static final String NOT_MODIFIED_NAME = "NOT_MODIFIED";

    private HttpResp() {
        throw new UnsupportedOperationException();
    }

    public static Response generic(HttpCode code, int statusCode, String statusName) {
        JsonObject object = new JsonObject();
        object.put("code", statusCode);
        object.put("name", statusName);
        return Response.of(code).content(HttpContentType.JSON, object);
    }

    public static Response generic(HttpCode code, int statusCode, String statusName, String message) {
        JsonObject object = new JsonObject();
        object.put("code", statusCode);
        object.put("name", statusName);
        object.put("message", message);
        return Response.of(code).content(HttpContentType.JSON, object);
    }

    public static Response generic(HttpCode code, int statusCode, String statusName, String additionalKey, IJson<?> additionalData) {
        JsonObject object = new JsonObject();
        object.put("code", statusCode);
        object.put("name", statusName);
        object.put(additionalKey, additionalData);
        return Response.of(code).content(HttpContentType.JSON, object);
    }

    public static Response generic(HttpCode code, int statusCode, String statusName, String message, String additionalKey, IJson<?> additionalData) {
        JsonObject object = new JsonObject();
        object.put("code", statusCode);
        object.put("name", statusName);
        object.put("message", message);
        object.put(additionalKey, additionalData);
        return Response.of(code).content(HttpContentType.JSON, object);
    }

    public static Response ok() {
        return generic(HttpCode.OK, OK, OK_NAME);
    }

    public static Response ok(String additionalKey, IJson<?> additionalData) {
        return generic(HttpCode.OK, OK, OK_NAME, additionalKey, additionalData);
    }

    public static Response unmodified() {
        return generic(HttpCode.OK, NOT_MODIFIED, NOT_MODIFIED_NAME);
    }

    public static Response unmodified(String message) {
        return generic(HttpCode.OK, NOT_MODIFIED, NOT_MODIFIED_NAME, message);
    }

    public static Response modified() {
        return generic(HttpCode.OK, MODIFIED, MODIFIED_NAME);
    }

    public static Response created() {
        return generic(HttpCode.CREATED, CREATED, CREATED_NAME);
    }

}
