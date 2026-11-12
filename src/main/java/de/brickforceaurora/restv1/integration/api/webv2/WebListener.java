package de.brickforceaurora.restv1.integration.api.webv2;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpHeaders;

final class WebListener extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Function<String, ObjectList<String>> CREATOR = (_) -> new ObjectArrayList<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(msg.uri());
        Map<String, List<String>> parameters = queryDecoder.parameters();
        Object2ObjectOpenHashMap<String, List<String>> map = new Object2ObjectOpenHashMap<>(msg.headers().size());
        for (Map.Entry<String, String> entry : msg.headers()) {
            map.computeIfAbsent(entry.getKey(), CREATOR).add(entry.getValue());
        }
        map.trim();

        String path = queryDecoder.path();
        HttpHeaders headers = new HttpHeaders(Object2ObjectMaps.unmodifiable(map));
        ByteBuf content = msg.content();
        
        
        
        // TODO: Continue
    }

}
