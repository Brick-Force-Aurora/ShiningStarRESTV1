package de.brickforceaurora.restv1.integration.api.web;

import me.lauriichan.snowframe.util.http.server.BasicHttpServer;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

final class WebProviderImpl implements IWebProvider {

    private final BasicHttpServer server;
    private final IAuthenticationProvider authenticationProvider;

    public WebProviderImpl(final BasicHttpServer server, final IAuthenticationProvider authenticationProvider) {
        this.server = server;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public <T> IWebContext<T> createContext(HttpContentType<T> contentType) {
        return new WebContextImpl<>(server, "", contentType, authenticationProvider);
    }

    @Override
    public <T> IWebContext<T> createContext(String basePath, HttpContentType<T> contentType) {
        return new WebContextImpl<>(server, basePath, contentType, authenticationProvider);
    }

}
