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
@Command(name = "move")
public class MoveCommand implements IUpdateCommand {

    @Action("")
    public void apply(UpdateActor actor, @Argument(name = "source target path", index = 0) String sourceTargetPath,
        @Argument(name = "target path", index = 1) String targetPath) throws IOException {
        IDataSource src = actor.target(sourceTargetPath);
        // This should not be simulated on the server, although it might cause issues
        actor.instructions().add("move \"" + sourceTargetPath + "\" \"" + targetPath + "\"");
        if (!src.exists()) {
            return;
        }
        src.delete();
    }

}
