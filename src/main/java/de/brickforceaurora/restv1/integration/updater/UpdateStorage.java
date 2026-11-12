package de.brickforceaurora.restv1.integration.updater;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.config.UpdaterConfig;
import de.brickforceaurora.restv1.config.common.ProductInfo;
import de.brickforceaurora.restv1.integration.updater.command.api.IUpdateCommand;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.ConfigModule;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.resource.source.IDataSource;

public final class UpdateStorage {

    static final Predicate<String> NOT_EMPTY_LINES = Predicate.not(String::isBlank);

    static final String INSTRUCTIONS_PATH = "update.instr";
    static final String UPDATE_ZIP_PATH = "update.zip";
    static final String CONTENTS_PATH = "contents";
    static final String BUNDLE_PATH = "%s_to_%s";

    private final Object2ObjectOpenHashMap<UUID, ProductStorage> products = new Object2ObjectOpenHashMap<>();

    private final CommandManager commandManager = new CommandManager();

    private final ISimpleLogger logger;
    private final UpdaterConfig config;
    
    private final IDataSource tmpSrc;
    private IDataSource bundledSrc, filesSrc;

    public UpdateStorage(SnowFrame<RestApp> frame) {
        this.logger = frame.logger();
        this.config = frame.module(ConfigModule.class).manager().config(UpdaterConfig.class);
        this.tmpSrc = frame.resource("tmp://updater");
        commandManager.setPrefix("");
        frame.extension(IUpdateCommand.class, false).callClasses(commandManager::register);
        updateStorageRoot(frame.resource(config.storageDirectory()));
    }
    
    public void updateStorageRoot(IDataSource source) {
        this.bundledSrc = source.resolve("bundled");
        this.filesSrc = source.resolve("files");
        for (ProductStorage storage : products.values()) {
            storage.updateStorageRoot(bundledSrc, filesSrc);
        }
    }

    final ISimpleLogger logger() {
        return logger;
    }

    final CommandManager commandManager() {
        return commandManager;
    }

    public ProductStorage ofProduct(UUID id) {
        ProductInfo info = config.get(id);
        if (info == null) {
            return null;
        }
        ProductStorage product = products.get(id);
        if (product == null) {
            product = new ProductStorage(this, info, tmpSrc, bundledSrc, filesSrc);
            products.put(id, product);
        }
        return product;
    }

    public ProductInfo productByName(String name) {
        return config.stream().filter(info -> info.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Stream<ProductInfo> productStream() {
        return config.stream();
    }

    public int productCount() {
        return config.count();
    }

}
