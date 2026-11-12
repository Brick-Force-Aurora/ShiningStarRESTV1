package de.brickforceaurora.restv1.integration.api.web;

import java.util.function.Consumer;

import com.sun.net.httpserver.HttpContext;

import me.lauriichan.snowframe.util.http.server.IHttpHandler;
import me.lauriichan.snowframe.util.http.server.IHttpSimpleHandler;

public interface IWebContext<T> {

    default void route(String path, IHttpSimpleHandler<T> handler, WebMethod... methods) {
        route(path, (IHttpHandler<T>) handler, methods);
    }

    default void route(String path, IHttpSimpleHandler<T> handler, Consumer<HttpContext> contextSetup, WebMethod... methods) {
        route(path, (IHttpHandler<T>) handler, contextSetup, methods);
    }

    void route(String path, IHttpHandler<T> handler, WebMethod... methods);

    void route(String path, IHttpHandler<T> handler, Consumer<HttpContext> contextSetup, WebMethod... methods);

}
