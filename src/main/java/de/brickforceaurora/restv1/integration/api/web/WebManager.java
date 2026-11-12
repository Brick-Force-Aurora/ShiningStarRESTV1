package de.brickforceaurora.restv1.integration.api.web;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.integration.api.IntegrationManager;
import de.brickforceaurora.restv1.config.ApiConfig;
import me.lauriichan.snowframe.ConfigModule;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.util.http.server.BasicHttpServer;

@Extension
public final class WebManager extends IntegrationManager {

    private final ApiConfig apiConfig;

    private volatile BasicHttpServer server;

    public WebManager(SnowFrame<RestApp> snowFrame) {
        super("web");
        this.apiConfig = snowFrame.module(ConfigModule.class).manager().config(ApiConfig.class);
    }

    @Override
    protected void onStart(SnowFrame<RestApp> snowFrame) throws Throwable {
        this.server = BasicHttpServer.builder().address(apiConfig.webHost()).port(apiConfig.webPort()).build();
        server.logger(snowFrame.logger());
        WebSignal.Setup setup = new WebSignal.Setup(new ApiKeyAuthenticationProvider(apiConfig));
        snowFrame.app().signalManager().call(setup);
        snowFrame.app().signalManager().call(new WebSignal.Start(new WebProviderImpl(server, setup.authenticationProvider())));
        server.delay(5).open();
    }

    @Override
    protected void onStop(SnowFrame<RestApp> snowFrame) throws Throwable {
        this.server.close();
        this.server = null;
        snowFrame.app().signalManager().call(new WebSignal.Stop());
    }

}
