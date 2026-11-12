package de.brickforceaurora.restv1.integration.api.web;

import java.util.Objects;
import java.util.function.Consumer;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.server.BasicHttpServer;
import me.lauriichan.snowframe.util.http.server.IHttpHandler;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

final class WebContextImpl<T> implements IWebContext<T> {

    public static final String BASE_PATH_PATTERN = "[a-zA-Z0-9_\\-/]+";
    public static final String PATH_PATTERN = "(\\/?[a-zA-Z0-9_\\-]+)*";

    private final BasicHttpServer server;
    private final String basePath;
    private final HttpContentType<T> contentType;
    private final IAuthenticationProvider authenticationProvider;

    public WebContextImpl(final BasicHttpServer server, String basePath, final HttpContentType<T> contentType,
        final IAuthenticationProvider authenticationProvider) {
        this.server = server;
        basePath = basePath.trim();
        if (!basePath.isEmpty()) {
            if (basePath.charAt(0) != '/') {
                basePath = '/' + basePath;
            }
            if (!basePath.matches(BASE_PATH_PATTERN)) {
                throw new IllegalArgumentException("Invalid base path: '%s'".formatted(basePath));
            }
        }
        this.basePath = basePath;
        this.contentType = contentType;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void route(String path, IHttpHandler<T> handler, WebMethod... methods) {
        routeInternal(path, handler, null, methods);
    }

    @Override
    public void route(String path, IHttpHandler<T> handler, Consumer<HttpContext> contextSetup, WebMethod... methods) {
        routeInternal(path, handler, contextSetup, methods);
    }

    private void routeInternal(String path, IHttpHandler<T> handler, Consumer<HttpContext> contextSetup, WebMethod[] methods) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");
        if (!(path = path.trim()).matches(PATH_PATTERN)) {
            throw new IllegalArgumentException("Invalid path: '" + path + "'");
        }
        HttpMethod[] httpMethods = new HttpMethod[methods.length];
        int i = 0;
        for (WebMethod method : methods) {
            httpMethods[i++] = method == null ? null : method.method();
        }
        if (!path.isEmpty() && path.charAt(0) != '/') {
            server.route(basePath + '/' + path, contentType, handler, adapt(contextSetup, methods), httpMethods);
            return;
        }
        server.route(basePath + path, contentType, handler, adapt(contextSetup, methods), httpMethods);
    }

    public String basePath() {
        return basePath;
    }

    private void setup(HttpContext context, WebMethod[] methods) {
        ObjectArrayList<String> handledMethods = new ObjectArrayList<>();
        for (WebMethod method : methods) {
            if (!method.authentication() || handledMethods.contains(method.method().name())) {
                continue;
            }
            handledMethods.add(method.method().name());
        }
        if (handledMethods.isEmpty()) {
            return;
        }
        Authenticator authenticator = authenticationProvider.create(context.getAuthenticator(), handledMethods);
        if (authenticator == null) {
            return;
        }
        context.setAuthenticator(authenticator);
    }

    private Consumer<HttpContext> adapt(Consumer<HttpContext> contextSetup, WebMethod[] methods) {
        if (contextSetup == null) {
            return ctx -> {
                setup(ctx, methods);
            };
        }
        return ctx -> {
            contextSetup.accept(ctx);
            setup(ctx, methods);
        };
    }

}
