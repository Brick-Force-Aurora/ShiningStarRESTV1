package de.brickforceaurora.restv1.command;

import de.brickforceaurora.restv1.Main;
import de.brickforceaurora.restv1.command.api.ICommand;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.snowframe.extension.Extension;

@Extension
@Command(name = "exit", description = "Stops the application")
public class ExitCommand implements ICommand {

    @Action("")
    @Description("Stops the application")
    public void stop() {
        Main.shutdown();
    }

}
