package de.brickforceaurora.restv1.integration.updater.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import de.brickforceaurora.restv1.integration.updater.UpdateInstructions;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public final class InstructionsContentType extends HttpContentType<UpdateInstructions> {

    public static final InstructionsContentType UPDATE_INSTRUCTIONS = new InstructionsContentType();

    private InstructionsContentType() {
        super("bfaurora/update-instructions", UpdateInstructions.class, "bfaurora/update-instructions");
    }

    @Override
    public UpdateInstructions read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException {
        Charset charset = Charset.forName(headers.getArguments("Content-Type").getOrDefault("charset", "utf-8"));
        return new UpdateInstructions(new String(inputStream.readAllBytes(), charset).split("\n"));
    }

    @Override
    public void write(IHeaderArgs typeArgs, FastByteArrayOutputStream outputStream, UpdateInstructions value) throws IOException {
        typeArgs.set("charset", "utf-8");
        outputStream.write(String.join("\n", value.instructions()).getBytes(StandardCharsets.UTF_8));
    }

}
