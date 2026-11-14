package de.brickforceaurora.restv1.integration.api.webv2;

import de.brickforceaurora.restv1.config.common.Server;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpCode;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.netty.IAuthenticator;
import me.lauriichan.snowframe.util.http.netty.server.HttpRouter;
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
            if (!methods.contains(channel.attr(HttpRouter.METHOD).get())) {
                return delegate == null ? null : delegate.authenticate(channel, headers);
            }
            String apiKey = headers.getValue("X-API-KEY");
            if (apiKey == null) {
                return Response.of(HttpCode.FORBIDDEN);
            }
            Server server = storage.serverByApiKey(apiKey);
            if (server == null) {
                return Response.of(HttpCode.FORBIDDEN);
            }
            channel.attr(WebManager.API_SERVER).set(server);
            return delegate == null ? null : delegate.authenticate(channel, headers);
        }

    }

}
