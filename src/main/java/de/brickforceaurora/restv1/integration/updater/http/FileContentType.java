package de.brickforceaurora.restv1.integration.updater.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.util.IOUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.resource.source.IDataSource;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.netty.type.NettyContentType;

public final class FileContentType extends NettyContentType<File> {

    public static final FileContentType FILE_TYPE = new FileContentType(RestApp.frame().resource("tmp://netty"));

    private final IDataSource location;

    private FileContentType(IDataSource location) {
        super("application/octet-stream", File.class, "*/*");
        this.location = location;
    }

    @Override
    public File readNetty(HttpHeaders headers, ByteBuf buffer) throws IOException {
        IDataSource location = newLocation();
        try (OutputStream output = location.openWritableStream()) {
            buffer.readBytes(output, buffer.readableBytes());
        }
        return IOUtil.asFile(location);
    }

    private IDataSource newLocation() {
        IDataSource source;
        while ((source = location.resolve(UUID.randomUUID().toString() + ".bin")).exists()) {
        }
        return source;
    }

    @Override
    public void writeNetty(IHeaderArgs typeArgs, File value, ObjectList<Object> output) throws IOException {
        output.add(value);
    }

}
