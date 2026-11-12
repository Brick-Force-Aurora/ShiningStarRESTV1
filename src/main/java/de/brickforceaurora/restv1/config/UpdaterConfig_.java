package de.brickforceaurora.restv1.config;

import java.util.UUID;
import java.util.stream.Stream;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.config.common.ProductInfo;
import de.brickforceaurora.restv1.integration.updater.UpdaterIntegration;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.config.Config;
import me.lauriichan.snowframe.config.ConfigValue;
import me.lauriichan.snowframe.config.Configuration;
import me.lauriichan.snowframe.config.IConfigHandler;
import me.lauriichan.snowframe.config.ISingleConfigExtension;
import me.lauriichan.snowframe.config.handler.JsonConfigHandler;
import me.lauriichan.snowframe.resource.source.IDataSource;

@Config(automatic = true)
public final class UpdaterConfig_ implements ISingleConfigExtension {

    private final Object2ObjectOpenHashMap<UUID, ProductInfo> products = new Object2ObjectOpenHashMap<>();
    private final SnowFrame<RestApp> snowFrame;

    @ConfigValue("storage.directory")
    public String storageDirectory = "data://updates";

    public UpdaterConfig_(SnowFrame<RestApp> snowFrame) {
        this.snowFrame = snowFrame;
    }

    @Override
    public IConfigHandler handler() {
        return JsonConfigHandler.JSON;
    }

    @Override
    public String path() {
        return "data://updater.json";
    }

    @Override
    public void onLoad(ISimpleLogger logger, Configuration configuration) throws Exception {
        Configuration serverConfigs = configuration.getConfiguration("products", true);
        for (String productId : serverConfigs.keySet()) {
            UUID id;
            try {
                id = UUID.fromString(productId);
            } catch (RuntimeException re) {
                logger.warning("Couldn't load product '{0}'", re, productId);
                continue;
            }
            Configuration productConfig = serverConfigs.getConfiguration(productId);
            String name = productConfig.get("name", String.class);
            products.put(id, new ProductInfo(id, name));
        }

        UpdaterIntegration integration = snowFrame.sharedExtensions().getCached(UpdaterIntegration.class);
        IDataSource storageSrc = snowFrame.resource(storageDirectory);
        try {
            storageSrc.createAsContainer();
        } catch (Exception exp) {
            logger.error("Failed to create update storage folder", exp);
        }
        if (integration != null) {
            integration.storage().updateStorageRoot(storageSrc);
        }
    }

    @Override
    public void onSave(ISimpleLogger logger, Configuration configuration) throws Exception {
        Configuration productConfigs = configuration.getConfiguration("products", true);
        for (ProductInfo product : products.values()) {
            Configuration productConfig = productConfigs.getConfiguration(product.id().toString(), true);
            productConfig.set("name", product.name());
        }
    }

    /*
     * Access
     */

    public ProductInfo newProduct(String name) {
        for (ProductInfo product : products.values()) {
            if (product.name().equalsIgnoreCase(name)) {
                return null;
            }
        }
        UUID id = newUUID();
        ProductInfo product = new ProductInfo(id, name);
        products.put(id, product);
        return product;
    }

    public ProductInfo get(UUID id) {
        return products.get(id);
    }

    public int count() {
        return products.size();
    }

    public Stream<ProductInfo> stream() {
        return products.values().stream();
    }

    /*
     * Helper
     */

    private UUID newUUID() {
        UUID id = UUID.randomUUID();
        while (products.containsKey(id)) {
            id = UUID.randomUUID();
        }
        return id;
    }

}
