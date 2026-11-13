package de.brickforceaurora.restv1.integration.api.webv2;

import me.lauriichan.snowframe.util.http.netty.IBodylessNettyHttpHandler;
import me.lauriichan.snowframe.util.http.netty.INettyHttpHandler;

public interface IWebContext<T> {

    void route(String path, IBodylessNettyHttpHandler handler, WebMethod... methods);

    void route(String path, INettyHttpHandler<T> handler, WebMethod... methods);

}
