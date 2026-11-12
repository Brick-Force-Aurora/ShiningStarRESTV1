package de.brickforceaurora.restv1.command;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.command.api.ICommand;
import de.brickforceaurora.restv1.config.common.ProductInfo;
import de.brickforceaurora.restv1.config.common.Server;
import de.brickforceaurora.restv1.config.ApiConfig;
import de.brickforceaurora.restv1.config.UpdaterConfig;
import me.lauriichan.snowframe.ConfigModule;
import me.lauriichan.snowframe.config.ConfigWrapper;
import me.lauriichan.snowframe.extension.Extension;

@Extension
@Command(name = "config", description = "Config management")
public class ConfigCommand implements ICommand {

    @Action("reload")
    @Description("Reloads all configs")
    public void configReload(Actor<?> actor) {
        RestApp.frame().module(ConfigModule.class).manager().reload();
        actor.sendMessage("Reloaded configs");
    }

    @Action("save")
    @Description("Saves all configs")
    public void configSave(Actor<?> actor) {
        RestApp.frame().module(ConfigModule.class).manager().save();
        actor.sendMessage("Saved configs");
    }

    @Action("server add")
    @Description("Adds a server")
    public void addServer(Actor<?> actor, String name) {
        ConfigWrapper<ApiConfig> apiConfig = RestApp.frame().module(ConfigModule.class).manager().wrapper(ApiConfig.class);
        Server server = apiConfig.config().newServer(name);
        if (server == null) {
            actor.sendMessage("There is already a server with the name '%s'".formatted(name));
            return;
        }
        actor.sendMessage("Added server '%s' with api key '%s'".formatted(name, server.apiKey()));
        apiConfig.save(true);
    }

    @Action("product add")
    @Description("Adds a product")
    public void addProduct(Actor<?> actor, String name) {
        ConfigWrapper<UpdaterConfig> updaterConfig = RestApp.frame().module(ConfigModule.class).manager().wrapper(UpdaterConfig.class);
        ProductInfo product = updaterConfig.config().newProduct(name);
        if (product == null) {
            actor.sendMessage("There is already a product with the name '%s'".formatted(name));
            return;
        }
        actor.sendMessage("Added product '%s' with id '%s'".formatted(name, product.idShort()));
        updaterConfig.save(true);
    }

}
