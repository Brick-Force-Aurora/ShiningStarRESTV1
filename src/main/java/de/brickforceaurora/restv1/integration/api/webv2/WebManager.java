package de.brickforceaurora.restv1.integration.api.webv2;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.config.ApiConfig;
import de.brickforceaurora.restv1.integration.api.IntegrationManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.nio.NioIoHandler;
import me.lauriichan.snowframe.ConfigModule;
import me.lauriichan.snowframe.SnowFrame;

public class WebManager extends IntegrationManager {

    private final ServerBootstrap bootstrap = new ServerBootstrap();

    private final MultiThreadIoEventLoopGroup group = new MultiThreadIoEventLoopGroup(Epoll.isAvailable() ? EpollIoHandler.newFactory() : NioIoHandler.newFactory());

    private final ApiConfig apiConfig;
    private final WebInit init;

    private volatile Channel serverChannel;

    public WebManager(SnowFrame<RestApp> snowFrame) {
        super("web");
        this.apiConfig = snowFrame.module(ConfigModule.class).manager().config(ApiConfig.class);
        this.init = new WebInit(apiConfig);
    }

    @Override
    protected void onStart(SnowFrame<RestApp> snowFrame) throws Throwable {
        bootstrap.group(group).localAddress(apiConfig.webHost(), apiConfig.webPort()).childOption(ChannelOption.TCP_NODELAY, true)
            .childHandler(init);
        this.serverChannel = bootstrap.bind().sync().channel();
    }

    @Override
    protected void onStop(SnowFrame<RestApp> snowFrame) throws Throwable {

    }

}
