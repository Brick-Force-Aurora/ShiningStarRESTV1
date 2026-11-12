package de.brickforceaurora.restv1.integration.api.webv2;

import de.brickforceaurora.restv1.config.ApiConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

final class WebInit extends ChannelInitializer<SocketChannel> {

    private final ApiConfig apiConfig;

    public WebInit(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Override
    protected void initChannel(final SocketChannel channel) throws Exception {
        final ChannelPipeline pipeline = channel.pipeline();

        // Inbound
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpObjectAggregator(apiConfig.webContentMaxLength(), true));
        pipeline.addLast(new HttpContentDecompressor(apiConfig.webContentCompressionMinLength()));
        pipeline.addLast(new WebListener());
        
        // Outbound
        pipeline.addLast(new HttpResponseEncoder());
    }

}
