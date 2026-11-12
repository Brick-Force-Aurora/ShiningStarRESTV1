package de.brickforceaurora.restv1.integration.updater.command;

import java.io.IOException;

import de.brickforceaurora.restv1.integration.updater.command.api.IUpdateCommand;
import de.brickforceaurora.restv1.integration.updater.command.api.UpdateActor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.resource.source.IDataSource;

@Extension
@Command(name = "copy")
public class CopyCommand implements IUpdateCommand {

    private static final String COPY_ZIP = "copy \"\" \"\"";

    @Action("")
    public void apply(UpdateActor actor, @Argument(name = "source path", index = 0) String sourcePath,
        @Argument(name = "target path", index = 1, optional = true) String targetPath) throws IOException {
        IDataSource src = actor.source(sourcePath);
        if (!src.exists()) {
            // We can't copy what isn't here
            return;
        }
        if (targetPath == null || targetPath.isBlank()) {
            targetPath = "";
        }
        src.transferTo(actor.target(targetPath));
        // We simply tell the client yeah everything is in the correct place
        // which it should be since we simulated it
        if (!actor.instructions().contains(COPY_ZIP)) {
            actor.instructions().add(COPY_ZIP);
            return;
        }
    }

}
