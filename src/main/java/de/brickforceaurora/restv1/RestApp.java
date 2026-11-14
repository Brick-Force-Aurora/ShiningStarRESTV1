package de.brickforceaurora.restv1;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.brickforceaurora.restv1.command.HelpCommand;
import de.brickforceaurora.restv1.command.api.ConsoleActor;
import de.brickforceaurora.restv1.command.api.ICommand;
import de.brickforceaurora.restv1.command.argument.IntegrationArgument;
import de.brickforceaurora.restv1.command.argument.LoggerArgumentProvider;
import de.brickforceaurora.restv1.integration.api.IntegrationManager;
import de.brickforceaurora.restv1.util.ExceptionCatchingExecutorService;
import de.brickforceaurora.restv1.util.console.Console;
import de.brickforceaurora.restv1.util.console.ConsoleInputHandler;
import me.lauriichan.laylib.command.ArgumentRegistry;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.ISnowFrameApp;
import me.lauriichan.snowframe.SignalModule;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.extension.IExtensionPool;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecyclePhase.Stage;
import me.lauriichan.snowframe.signal.SignalManager;
import me.lauriichan.snowframe.util.logger.FileLogger;

public class RestApp implements ISnowFrameApp<RestApp> {

    public static final ISimpleLogger LOGGER = new FileLogger(new File("logs"), Console.INSTANCE);

    public static final ScheduledExecutorService SCHEDULER = new ExceptionCatchingExecutorService(
        Executors.newScheduledThreadPool(2, Thread.ofVirtual().name("Scheduler", 0).factory()), LOGGER);

    private static SnowFrame<RestApp> snowFrame;
    private static ConsoleActor actor;

    static SnowFrame<RestApp> init(final String[] args) {
        if (snowFrame != null) {
            return snowFrame;
        }

        snowFrame = SnowFrame.builder(new RestApp()).logger(LOGGER).build();
        actor = new ConsoleActor(snowFrame);
        return snowFrame;
    }

    public static RestApp get() {
        return snowFrame.app();
    }

    public static SnowFrame<RestApp> frame() {
        return snowFrame;
    }

    public static ConsoleActor actor() {
        return actor;
    }

    private CommandManager commandManager;
    private SignalManager signalManager;

    private ConsoleInputHandler inputHandler;

    private IExtensionPool<IntegrationManager> integrations;

    @Override
    public SnowFrame<RestApp> snowFrame() {
        return snowFrame;
    }

    @Override
    public void registerLifecycle(Lifecycle<RestApp> lifecycle) {
        lifecycle.startupChain().register("load", Stage.PRE, frame -> {
            frame.resourceManager().register("data", Paths.get("data"));
            frame.resourceManager().register("tmp", Paths.get("tmp"));
            frame.resource("tmp://").delete();
        }).register("load", Stage.POST, frame -> {
            commandManager = new CommandManager(frame.logger());

            ArgumentRegistry arguments = commandManager.getRegistry();
            arguments.registerArgumentType(LoggerArgumentProvider.class);
            arguments.registerArgumentType(IntegrationArgument.class);

            frame.extension(ICommand.class, false).callClasses(commandManager::register);

            signalManager = frame.module(SignalModule.class).signalManager();
        }).register("reload_config", Stage.POST, _ -> {

        }).register("ready", Stage.POST, frame -> {
            inputHandler = new ConsoleInputHandler(actor, commandManager);

            integrations = frame.extension(IntegrationManager.class, true);
            integrations.extensions().forEach(manager -> manager.start());

            HelpCommand.doHelp(actor, commandManager);
        });
        lifecycle.shutdownChain().register("shutdown", Stage.MAIN, _ -> {
            integrations.extensions().forEach(manager -> manager.stop());
        }).register("shutdown", Stage.POST, _ -> {
            Console.INSTANCE.close();
            ((FileLogger) LOGGER).close();
        });
    }

    public CommandManager commandManager() {
        return commandManager;
    }

    public SignalManager signalManager() {
        return signalManager;
    }

    public ConsoleInputHandler inputHandler() {
        return inputHandler;
    }

    public IExtensionPool<IntegrationManager> integrations() {
        return integrations;
    }

}
