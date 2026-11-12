package de.brickforceaurora.restv1.util.console;

import java.util.Arrays;

import de.brickforceaurora.restv1.Main;
import de.brickforceaurora.restv1.command.api.ConsoleActor;
import de.brickforceaurora.restv1.command.message.CommandManagerMessage;
import de.brickforceaurora.restv1.util.CLIUtil;
import de.brickforceaurora.restv1.util.StringReader;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.CommandProcess;
import me.lauriichan.laylib.command.Node;
import me.lauriichan.laylib.command.NodeAction;
import me.lauriichan.laylib.command.NodeArgument;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.reflection.ClassUtil;

public final class ConsoleInputHandler {

    private final Console console = Console.INSTANCE;

    private final ConsoleActor actor;
    private final CommandManager commandManager;

    public ConsoleInputHandler(ConsoleActor actor, CommandManager commandManager) {
        this.actor = actor;
        this.commandManager = commandManager;
    }

    public void commandLoop() {
        while (!Main.isShutdown()) {
            String line = console.readLine();
            if (line.isBlank()) {
                continue;
            }
            String[] args = CLIUtil.toArguments(line);
            runCommand(args[0], Arrays.copyOfRange(args, 1, args.length));
        }
    }

    private void runCommand(String commandName, String[] args) {
        final Triple<NodeCommand, Node, String> triple = commandManager.findNode(commandName, args);
        if (triple == null) {
            actor.sendTranslatedMessage("command.process.create.no-command", Key.of("command", commandName));
            return;
        }
        final NodeCommand command = triple.getA();
        if (command.isRestricted() && !actor.hasPermission(command.getPermission())) {
            actor.sendTranslatedMessage("command.process.not-permitted", Key.of("permission", command.getPermission()));
            return;
        }
        final Node node = triple.getB();
        final String commandPath = triple.getC().substring(1);
        if (node.getAction() == null) {
            actor.sendTranslatedMessage("command.process.create.no-action", Key.of("command", commandPath));
            return;
        }
        final NodeAction action = node.getAction();
        if (action.isRestricted() && !actor.hasPermission(action.getPermission())) {
            actor.sendTranslatedMessage("command.process.not-permitted", Key.of("permission", action.getPermission()));
            return;
        }
        final CommandProcess process = new CommandProcess(triple.getC(), action, command.getInstance());
        NodeArgument argument = process.findNext(actor);
        if (argument == null) {
            commandManager.executeProcess(actor, process);
            return;
        }
        final int space = CLIUtil.countSpace(commandPath);
        int argIdx = 0;
        for (int index = 0; index < space; index++) {
            while (args[argIdx++].isEmpty()) {
            }
        }
        final StringBuilder string = new StringBuilder();
        for (int index = argIdx; index < args.length; index++) {
            string.append(args[index]);
            if (index + 1 != args.length) {
                string.append(' ');
            }
        }
        final StringReader reader = new StringReader(string.toString());
        String data;
        while (argument != null) {
            if (!reader.skipWhitespace().hasNext()) {
                if (argument.isOptional()) {
                    process.skip(actor);
                    argument = process.findNext(actor);
                    continue;
                }
                actor.sendTranslatedMessage(CommandManagerMessage.INPUT_FAILED.id(),
                    Key.of("argument.type", ClassUtil.getClassName(argument.getArgumentType())), Key.of("error", "Argument not provided!"));
                return;
            }
            data = reader.read();
            try {
                process.provide(actor, data);
            } catch (final IllegalArgumentException exp) {
                actor.sendTranslatedMessage(CommandManagerMessage.INPUT_FAILED.id(),
                    Key.of("argument.type", ClassUtil.getClassName(argument.getArgumentType())), Key.of("error", exp.getMessage()));
                return;
            }
            argument = process.findNext(actor);
        }
        commandManager.executeProcess(actor, process);
    }

}
