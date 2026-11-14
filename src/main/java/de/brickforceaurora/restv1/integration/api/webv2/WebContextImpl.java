package de.brickforceaurora.restv1.integration.api.webv2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.netty.HttpNode;
import me.lauriichan.snowframe.util.http.netty.IAuthenticator;
import me.lauriichan.snowframe.util.http.netty.IBodylessNettyHttpHandler;
import me.lauriichan.snowframe.util.http.netty.INettyHttpHandler;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public class WebContextImpl<T> implements IWebContext<T> {

    private final HttpNode base;
    private final HttpContentType<T> contentType;
    private final IAuthenticationProvider authenticationProvider;

    public WebContextImpl(HttpNode node, HttpContentType<T> contentType, IAuthenticationProvider authenticationProvider) {
        this.base = node;
        this.contentType = contentType;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void route(String path, INettyHttpHandler<T> handler, WebMethod... methods) {
        ObjectArrayList<HttpMethod> authMethods = new ObjectArrayList<>(methods.length);
        HttpNode node = this.base.add(path);
        for (WebMethod method : methods) {
            if (method == null) {
                continue;
            }
            if (method.authentication() && authenticationProvider != null) {
                authMethods.add(method.method());
                continue;
            }
            node.handler(method.method(), contentType, handler);
        }
        if (!authMethods.isEmpty()) {
            authMethods.trim();
            IAuthenticator authenticator = authenticationProvider.create(null, ObjectLists.unmodifiable(authMethods));
            for (HttpMethod method : authMethods) {
                node.handler(method, authenticator, contentType, handler);
            }
        }
    }

    @Override
    public void route(String path, IBodylessNettyHttpHandler handler, WebMethod... methods) {
        ObjectArrayList<HttpMethod> authMethods = new ObjectArrayList<>(methods.length);
        HttpNode node = this.base.add(path);
        for (WebMethod method : methods) {
            if (method == null) {
                continue;
            }
            if (method.authentication() && authenticationProvider != null) {
                authMethods.add(method.method());
                continue;
            }
            node.handler(method.method(), handler);
        }
        if (!authMethods.isEmpty()) {
            authMethods.trim();
            IAuthenticator authenticator = authenticationProvider.create(null, ObjectLists.unmodifiable(authMethods));
            for (HttpMethod method : authMethods) {
                node.handler(method, authenticator, handler);
            }
        }
    }

}
