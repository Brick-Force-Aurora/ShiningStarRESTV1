package de.brickforceaurora.restv1.config;

import java.security.SecureRandom;
import java.util.UUID;

import de.brickforceaurora.restv1.config.common.Server;
import de.brickforceaurora.restv1.integration.api.web.ApiKeyAuthenticationProvider.IApiKeyStorage;
import de.brickforceaurora.restv1.util.RandomUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.config.Config;
import me.lauriichan.snowframe.config.ConfigValue;
import me.lauriichan.snowframe.config.Configuration;
import me.lauriichan.snowframe.config.IConfigHandler;
import me.lauriichan.snowframe.config.ISingleConfigExtension;
import me.lauriichan.snowframe.config.handler.JsonConfigHandler;

@Config(automatic = true)
public final class ApiConfig_ implements ISingleConfigExtension, IApiKeyStorage {

    private static final SecureRandom KEY_RANDOM = new SecureRandom();

    private final Object2ObjectOpenHashMap<String, Server> apiKeyToServer = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<UUID, Server> servers = new Object2ObjectOpenHashMap<>();

    @ConfigValue("web.host")
    public String webHost = "0.0.0.0";
    @ConfigValue("web.port")
    public int webPort = 5005;
    @ConfigValue("web.content.max_length")
    public int webContentMaxLength = 1024 * 1024 * 512; // Half a GigiByte
    @ConfigValue("web.content.compression.min_length")
    public int webContentCompressionMinLength = 1024 * 1024 * 16; // 16 MebiBytes

    @ConfigValue("logger.debug")
    public boolean debug = false;
    @ConfigValue("logger.trace")
    public boolean trace = false;

    @Override
    public IConfigHandler handler() {
        return JsonConfigHandler.JSON;
    }

    @Override
    public String path() {
        return "data://api.json";
    }

    @Override
    public void onLoad(ISimpleLogger logger, Configuration configuration) throws Exception {
        logger.setDebug(debug);
        logger.setTracking(trace);
        apiKeyToServer.clear();
        servers.clear();
        Configuration serverConfigs = configuration.getConfiguration("servers", true);
        for (String serverId : serverConfigs.keySet()) {
            UUID id;
            try {
                id = UUID.fromString(serverId);
            } catch (RuntimeException re) {
                logger.warning("Couldn't load server '{0}'", re, serverId);
                continue;
            }
            Configuration serverConfig = serverConfigs.getConfiguration(serverId);
            String name = serverConfig.get("name", String.class);
            String apiKey = serverConfig.get("api_key", String.class);
            if (name == null || name.isBlank()) {
                logger.warning("Invalid server name '{0}' for server '{1}'", name, serverId);
                continue;
            }
            if (apiKey == null || apiKey.length() != 96) {
                apiKey = newApiKey(id);
            }
            if (apiKeyToServer.containsKey(apiKey)) {
                logger.warning("Invalid server '{0}', api key is duplicated", serverId);
                continue;
            }
            Server server = new Server(id, name, apiKey);
            apiKeyToServer.put(apiKey, server);
            servers.put(id, server);
        }
    }

    @Override
    public void onSave(ISimpleLogger logger, Configuration configuration) throws Exception {
        Configuration serverConfigs = configuration.getConfiguration("servers", true);
        for (Server server : servers.values()) {
            Configuration serverConfig = serverConfigs.getConfiguration(server.id().toString(), true);
            serverConfig.set("name", server.name());
            serverConfig.set("api_key", server.apiKey());
        }
    }

    /*
     * Access
     */

    public Server newServer(String name) {
        for (Server server : servers.values()) {
            if (server.name().equalsIgnoreCase(name)) {
                return null;
            }
        }
        UUID id = newUUID();
        Server server = new Server(id, name, newApiKey(id));
        servers.put(id, server);
        apiKeyToServer.put(server.apiKey(), server);
        return server;
    }

    public Server get(UUID id) {
        return servers.get(id);
    }

    @Override
    public Server serverByApiKey(String apiKey) {
        return apiKeyToServer.get(apiKey);
    }

    /*
     * Helper
     */

    private UUID newUUID() {
        UUID id = UUID.randomUUID();
        while (servers.containsKey(id)) {
            id = UUID.randomUUID();
        }
        return id;
    }

    private String newApiKey(UUID id) {
        return RandomUtil.generateCode(KEY_RANDOM, apiKeyToServer::containsKey, id, 96);
    }

}
