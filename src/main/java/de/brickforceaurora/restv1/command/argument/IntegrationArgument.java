package de.brickforceaurora.restv1.command.argument;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.integration.api.IntegrationManager;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;

public class IntegrationArgument implements IArgumentType<IntegrationManager> {

    @Override
    public IntegrationManager parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        IntegrationManager manager = RestApp.get().integrations().extensions().stream()
            .filter(integration -> integration.name().equalsIgnoreCase(input)).findFirst().orElse(null);
        if (manager == null) {
            throw new IllegalArgumentException("Unknown integration '%s'".formatted(input));
        }
        return manager;
    }

}
