package de.brickforceaurora.restv1.integration.api.web;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;

import de.brickforceaurora.restv1.config.common.Server;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpCode;

public final class ApiKeyAuthenticationProvider implements IAuthenticationProvider {

    private final IApiKeyStorage storage;

    public ApiKeyAuthenticationProvider(IApiKeyStorage storage) {
        this.storage = storage;
    }

    @Override
    public Authenticator create(Authenticator delegate, ObjectList<String> methods) {
        return new ApiKeyAuthenticator(storage, delegate, methods);
    }

    public static interface IApiKeyStorage {

        Server serverByApiKey(String apiKey);

    }

    private static class ApiKeyAuthenticator extends Authenticator {

        private final IApiKeyStorage storage;

        private final Authenticator delegate;
        private final ObjectList<String> methods;

        public ApiKeyAuthenticator(final IApiKeyStorage storage, final Authenticator delegate, final ObjectList<String> methods) {
            this.storage = storage;
            this.delegate = delegate;
            this.methods = methods;
        }

        @Override
        public Result authenticate(HttpExchange exch) {
            if (!methods.contains(exch.getRequestMethod())) {
                return delegate == null ? new Success(null) : delegate.authenticate(exch);
            }
            String string = exch.getRequestHeaders().getFirst("X-API-KEY");
            if (string == null) {
                return new Failure(HttpCode.FORBIDDEN.code());
            }
            Server server = storage.serverByApiKey(string);
            if (server == null) {
                return new Failure(HttpCode.FORBIDDEN.code());
            }
            exch.setAttribute(Server.ATTR, server);
            return delegate == null ? new Success(null) : delegate.authenticate(exch);
        }

    }

}
