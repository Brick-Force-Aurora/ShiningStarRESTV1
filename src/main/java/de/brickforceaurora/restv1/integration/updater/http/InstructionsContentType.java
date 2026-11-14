package de.brickforceaurora.restv1.integration.updater.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import de.brickforceaurora.restv1.integration.updater.UpdateInstructions;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.netty.type.NettyContentType;

public final class InstructionsContentType extends NettyContentType<UpdateInstructions> {

    public static final InstructionsContentType UPDATE_INSTRUCTIONS = new InstructionsContentType();

    private InstructionsContentType() {
        super("bfaurora/update-instructions", UpdateInstructions.class, "bfaurora/update-instructions");
    }

    @Override
    public Charset charset() {
        return StandardCharsets.UTF_8;
    }

    @Override
    public UpdateInstructions readNetty(HttpHeaders headers, ByteBuf buffer) throws IOException {
        return new UpdateInstructions(buffer.toString(charsetFor(headers)).split("\n"));
    }

    @Override
    public void writeNetty(IHeaderArgs typeArgs, UpdateInstructions value, ObjectList<Object> output) throws IOException {
        typeArgs.set("charset", charset().name());
        output.add(String.join("\n", value.instructions()));
    }

}
