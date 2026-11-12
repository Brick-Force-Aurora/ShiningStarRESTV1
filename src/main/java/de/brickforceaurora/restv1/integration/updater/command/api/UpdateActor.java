package de.brickforceaurora.restv1.integration.updater.command.api;

import java.util.UUID;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.snowframe.resource.source.IDataSource;
import me.lauriichan.snowframe.util.attribute.Attributes;

public final class UpdateActor extends Actor<IDataSource> {
    
    public static record Instruction(int priority, String instruction) {}

    public static final String ATTR_SOURCE = "source";
    public static final String ATTR_INSTRUCTION_DELETE = "instr/delete";
    public static final String ATTR_INSTRUCTION_ANY = "instr/*";

    private static final MessageManager MNG = new MessageManager();

    public final Attributes attributes = new Attributes();

    public UpdateActor(IDataSource handle) {
        super(handle, MNG);
    }

    @Override
    public UUID getId() {
        return Actor.IMPL_ID;
    }

    @Override
    public String getName() {
        return getHandle().getPath();
    }

    @SuppressWarnings("unchecked")
    public ObjectArrayList<String> deleteInstructions() {
        return attributes.attr(ATTR_INSTRUCTION_DELETE, ObjectArrayList.class);
    }

    @SuppressWarnings("unchecked")
    public ObjectArrayList<String> instructions() {
        return attributes.attr(ATTR_INSTRUCTION_ANY, ObjectArrayList.class);
    }

    public IDataSource source() {
        return attributes.attr(ATTR_SOURCE, IDataSource.class);
    }

    public IDataSource source(String path) {
        if (path.contains("..")) {
            path = path.replace("../", "");
            if (path.contains("..")) {
                path = path.replace("..", "");
            }
        }
        return source().resolve(path);
    }

    public IDataSource target() {
        return getHandle();
    }

    public IDataSource target(String path) {
        if (path.contains("..")) {
            path = path.replace("../", "");
            if (path.contains("..")) {
                path = path.replace("..", "");
            }
        }
        return target().resolve(path);
    }

}
