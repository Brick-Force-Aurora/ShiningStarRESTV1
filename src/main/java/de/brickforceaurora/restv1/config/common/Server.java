package de.brickforceaurora.restv1.config.common;

import java.util.UUID;

import com.sun.net.httpserver.HttpExchange;

/*
 * Records have to be below the config values or class generation fucks up
 */
public record Server(UUID id, String name, String apiKey) {

    public static final String ATTR = "ApiServer";

    public static Server of(HttpExchange exchange) {
        return (Server) exchange.getAttribute(Server.ATTR);
    }

}