package de.brickforceaurora.restv1.command;

import de.brickforceaurora.restv1.command.api.ICommand;
import de.brickforceaurora.restv1.integration.api.IntegrationManager;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.snowframe.extension.Extension;

@Extension
@Command(name = "integration", description = "Restart integrations")
public class IntegrationCommand implements ICommand {

    @Action("status")
    public void status(Actor<?> actor, IntegrationManager manager) {
        if (manager.isRunning()) {
            actor.sendMessage("Integration '%s' is currently running".formatted(manager.name()));
            return;
        }
        actor.sendMessage("Integration '%s' is currently not running".formatted(manager.name()));
    }

    @Action("start")
    public void start(Actor<?> actor, IntegrationManager manager) {
        if (manager.isRunning()) {
            actor.sendMessage("Integration '%s' is not running".formatted(manager.name()));
            return;
        }
        manager.start();
        if (manager.isRunning()) {
            actor.sendMessage("Integration '%s' was successfully started".formatted(manager.name()));
            return;
        }
        actor.sendMessage("Failed to start integration '%s'".formatted(manager.name()));
    }

    @Action("restart")
    public void restart(Actor<?> actor, IntegrationManager manager) {
        if (manager.isRunning()) {
            manager.stop();
        }
        manager.start();
        if (manager.isRunning()) {
            actor.sendMessage("Integration '%s' was successfully restarted".formatted(manager.name()));
            return;
        }
        actor.sendMessage("Failed to restart integration '%s'".formatted(manager.name()));
    }

    @Action("stop")
    public void stop(Actor<?> actor, IntegrationManager manager) {
        if (!manager.isRunning()) {
            actor.sendMessage("Integration '%s' is not running".formatted(manager.name()));
            return;
        }
        manager.stop();
        actor.sendMessage("Integration '%s' was successfully stopped".formatted(manager.name()));
    }

}
