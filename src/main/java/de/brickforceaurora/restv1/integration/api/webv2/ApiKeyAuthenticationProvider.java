package de.brickforceaurora.restv1.integration.api.webv2;

import de.brickforceaurora.restv1.config.common.Server;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.netty.IAuthenticator;
import me.lauriichan.snowframe.util.http.server.Response;

public final class ApiKeyAuthenticationProvider implements IAuthenticationProvider {

    private final IApiKeyStorage storage;

    public ApiKeyAuthenticationProvider(IApiKeyStorage storage) {
        this.storage = storage;
    }

    @Override
    public IAuthenticator create(IAuthenticator delegate, ObjectList<HttpMethod> methods) {
        return new ApiKeyAuthenticator(storage, delegate, methods);
    }

    public static interface IApiKeyStorage {

        Server serverByApiKey(String apiKey);

    }

    private static class ApiKeyAuthenticator implements IAuthenticator {

        private final IApiKeyStorage storage;

        private final IAuthenticator delegate;
        private final ObjectList<HttpMethod> methods;

        public ApiKeyAuthenticator(final IApiKeyStorage storage, final IAuthenticator delegate, final ObjectList<HttpMethod> methods) {
            this.storage = storage;
            this.delegate = delegate;
            this.methods = methods;
        }
        
        @Override
        public Response authenticate(Channel channel, HttpHeaders headers) {
            return null;
        }

//        @Override
//        public Result authenticate(HttpExchange exch) {
//            if (!methods.contains(exch.getRequestMethod())) {
//                return delegate == null ? new Success(null) : delegate.authenticate(exch);
//            }
//            String string = exch.getRequestHeaders().getFirst("X-API-KEY");
//            if (string == null) {
//                return new Failure(HttpCode.FORBIDDEN.code());
//            }
//            Server server = storage.serverByApiKey(string);
//            if (server == null) {
//                return new Failure(HttpCode.FORBIDDEN.code());
//            }
//            exch.setAttribute(Server.ATTR, server);
//            return delegate == null ? new Success(null) : delegate.authenticate(exch);
//        }

    }

}
