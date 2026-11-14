package de.brickforceaurora.restv1.integration.api.webv2;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.config.ApiConfig;
import de.brickforceaurora.restv1.config.common.Server;
import de.brickforceaurora.restv1.integration.api.IntegrationManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import me.lauriichan.snowframe.ConfigModule;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.util.http.netty.HttpNode;
import me.lauriichan.snowframe.util.http.netty.server.HttpInit;
import me.lauriichan.snowframe.util.http.netty.server.IHttpConfig;

@Extension
public class WebManager extends IntegrationManager {

    public static final AttributeKey<Server> API_SERVER = AttributeKey.valueOf("API_SERVER");

    private final ServerBootstrap bootstrap = new ServerBootstrap();

    private final MultiThreadIoEventLoopGroup group = new MultiThreadIoEventLoopGroup(
        Epoll.isAvailable() ? EpollIoHandler.newFactory() : NioIoHandler.newFactory());

    private final ApiConfig apiConfig;
    private final HttpInit init;

    private final HttpNode root = new HttpNode();

    private volatile Channel serverChannel;

    public WebManager(SnowFrame<RestApp> snowFrame) {
        super("web");
        this.apiConfig = snowFrame.module(ConfigModule.class).manager().config(ApiConfig.class);
        this.init = new HttpInit(snowFrame.logger(), new IHttpConfig() {
            @Override
            public boolean enableDecompression() {
                return true;
            }

            @Override
            public int decompressionMaxLength() {
                return apiConfig.webContentCompressionMinLength();
            }

            @Override
            public int maxLength() {
                return apiConfig.webContentMaxLength();
            }
        }, root);
    }

    @Override
    protected void onStart(SnowFrame<RestApp> snowFrame) throws Throwable {
        bootstrap.group(group).localAddress(apiConfig.webHost(), apiConfig.webPort()).childOption(ChannelOption.TCP_NODELAY, true)
            .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class).childHandler(init);
        root.reset();
        WebSignal.Setup setup = new WebSignal.Setup(new ApiKeyAuthenticationProvider(apiConfig));
        snowFrame.app().signalManager().call(setup);
        snowFrame.app().signalManager().call(new WebSignal.Start(new WebProviderImpl(root, setup.authenticationProvider())));
        this.serverChannel = bootstrap.bind().sync().channel();
    }

    @Override
    protected void onStop(SnowFrame<RestApp> snowFrame) throws Throwable {
        this.serverChannel.close().sync();
        this.serverChannel = null;
        snowFrame.app().signalManager().call(new WebSignal.Stop());
    }

}
