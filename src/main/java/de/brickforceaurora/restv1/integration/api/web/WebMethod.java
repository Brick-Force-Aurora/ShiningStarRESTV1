package de.brickforceaurora.restv1.integration.api.web;

import me.lauriichan.snowframe.util.http.HttpMethod;

public final class WebMethod {

    private static final WebMethod[] GET = create(HttpMethod.GET);
    private static final WebMethod[] HEAD = create(HttpMethod.HEAD);
    private static final WebMethod[] POST = create(HttpMethod.POST);
    private static final WebMethod[] PUT = create(HttpMethod.PUT);
    private static final WebMethod[] DELETE = create(HttpMethod.DELETE);
    private static final WebMethod[] CONNECT = create(HttpMethod.CONNECT);
    private static final WebMethod[] OPTIONS = create(HttpMethod.OPTIONS);
    private static final WebMethod[] TRACE = create(HttpMethod.TRACE);
    private static final WebMethod[] PATCH = create(HttpMethod.PATCH);

    private static WebMethod[] create(HttpMethod method) {
        return new WebMethod[] {
            new WebMethod(method, false),
            new WebMethod(method, true)
        };
    }

    private final HttpMethod method;
    private final boolean authentication;

    private WebMethod(HttpMethod method, boolean authentication) {
        this.method = method;
        this.authentication = authentication;
    }

    public HttpMethod method() {
        return method;
    }

    public boolean authentication() {
        return authentication;
    }

    /*
     * Http method
     */

    public static WebMethod get() {
        return GET[1];
    }

    public static WebMethod get(boolean authenticated) {
        return GET[authenticated ? 1 : 0];
    }

    public static WebMethod head() {
        return HEAD[1];
    }

    public static WebMethod head(boolean authenticated) {
        return HEAD[authenticated ? 1 : 0];
    }

    public static WebMethod post() {
        return POST[1];
    }

    public static WebMethod post(boolean authenticated) {
        return POST[authenticated ? 1 : 0];
    }

    public static WebMethod put() {
        return PUT[1];
    }

    public static WebMethod put(boolean authenticated) {
        return PUT[authenticated ? 1 : 0];
    }

    public static WebMethod delete() {
        return DELETE[1];
    }

    public static WebMethod delete(boolean authenticated) {
        return DELETE[authenticated ? 1 : 0];
    }

    public static WebMethod connect() {
        return CONNECT[1];
    }

    public static WebMethod connect(boolean authenticated) {
        return CONNECT[authenticated ? 1 : 0];
    }

    public static WebMethod options() {
        return OPTIONS[1];
    }

    public static WebMethod options(boolean authenticated) {
        return OPTIONS[authenticated ? 1 : 0];
    }

    public static WebMethod trace() {
        return TRACE[1];
    }

    public static WebMethod trace(boolean authenticated) {
        return TRACE[authenticated ? 1 : 0];
    }

    public static WebMethod patch() {
        return PATCH[1];
    }

    public static WebMethod patch(boolean authenticated) {
        return PATCH[authenticated ? 1 : 0];
    }

}
