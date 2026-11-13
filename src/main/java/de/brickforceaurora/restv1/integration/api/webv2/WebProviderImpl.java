package de.brickforceaurora.restv1.integration.api.webv2;

import me.lauriichan.snowframe.util.http.netty.HttpNode;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

final class WebProviderImpl implements IWebProvider {
    
    public static final String BASE_PATH_PATTERN = "[a-zA-Z0-9_\\-/]+";

    private final HttpNode root;
    private final IAuthenticationProvider authenticationProvider;

    public WebProviderImpl(final HttpNode root, final IAuthenticationProvider authenticationProvider) {
        this.root = root;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public <T> IWebContext<T> createContext(HttpContentType<T> contentType) {
        return new WebContextImpl<>(root, contentType, authenticationProvider);
    }

    @Override
    public <T> IWebContext<T> createContext(String basePath, HttpContentType<T> contentType) {
        return new WebContextImpl<>(root.add(basePath), contentType, authenticationProvider);
    }

}
