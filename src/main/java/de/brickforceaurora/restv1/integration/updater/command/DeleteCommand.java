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
@Command(name = "delete")
public class DeleteCommand implements IUpdateCommand {

    @Action("")
    public void apply(UpdateActor actor, @Argument(name = "target path", index = 0) String targetPath)
        throws IOException {
        IDataSource src = actor.target(targetPath);
        if (!src.exists()) {
            actor.deleteInstructions().add("delete \"" + targetPath + '"');
            return;
        }
        src.delete();
    }

}
