package de.brickforceaurora.restv1.command.api;

import java.util.UUID;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.command.message.CommandManagerMessage;
import de.brickforceaurora.restv1.command.message.SimpleMessageProviderFactory;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.localization.source.EnumMessageSource;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.util.logger.FileLogger;

public class ConsoleActor extends Actor<SnowFrame<RestApp>> {

    private static final String COMMAND_LOG_FORMAT = "[%s][COMMAND/%s] %s";

    private final FileLogger fileLogger;

    public ConsoleActor(SnowFrame<RestApp> frame) {
        MessageManager messageManager = new MessageManager();
        messageManager.register(new EnumMessageSource(CommandManagerMessage.class, new SimpleMessageProviderFactory()));
        super(frame, messageManager);
        this.fileLogger = (FileLogger) frame.logger();
    }

    @Override
    public UUID getId() {
        return Actor.IMPL_ID;
    }

    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public void sendMessage(String message) {
        fileLogger.print(COMMAND_LOG_FORMAT, message);
    }

}
