package de.brickforceaurora.restv1.integration.api.webv2;

import java.util.Objects;

import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.netty.HttpNode;
import me.lauriichan.snowframe.util.http.netty.IBodylessNettyHttpHandler;
import me.lauriichan.snowframe.util.http.netty.INettyHttpHandler;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public class WebContextImpl<T> implements IWebContext<T> {

    private final HttpNode node;
    private final HttpContentType<T> contentType;
    private final IAuthenticationProvider authenticationProvider;

    public WebContextImpl(HttpNode node, HttpContentType<T> contentType, IAuthenticationProvider authenticationProvider) {
        this.node = node;
        this.contentType = contentType;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void route(String path, INettyHttpHandler<T> handler, WebMethod... methods) {
        routeInternal(path, handler, methods);
    }

    private void routeInternal(String path, INettyHttpHandler<T> handler, WebMethod[] methods) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");
        node.add(path);
        
    }

    @Override
    public void route(String path, IBodylessNettyHttpHandler handler, WebMethod... methods) {
        // TODO Auto-generated method stub
        
    }

}
