package de.brickforceaurora.restv1.integration.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.brickforceaurora.restv1.RestApp;
import de.brickforceaurora.restv1.config.common.ProductInfo;
import de.brickforceaurora.restv1.integration.updater.command.api.UpdateActor;
import de.brickforceaurora.restv1.integration.updater.http.FileContentType;
import de.brickforceaurora.restv1.integration.updater.http.InstructionsContentType;
import de.brickforceaurora.restv1.util.CLIUtil;
import de.brickforceaurora.restv1.util.IOUtil;
import de.brickforceaurora.restv1.util.StringReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.command.CommandProcess;
import me.lauriichan.laylib.command.Node;
import me.lauriichan.laylib.command.NodeArgument;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.snowframe.resource.source.IDataSource;
import me.lauriichan.snowframe.util.Version;
import me.lauriichan.snowframe.util.http.data.MultiFormData;

public final class ProductStorage {

    private final UpdateStorage storage;

    private final ProductInfo info;

    private final Object2ObjectMap<Version, UpdateInstructions> instructionMap = Object2ObjectMaps
        .synchronize(new Object2ObjectOpenHashMap<>());
    private final ObjectList<Version> versions = ObjectLists.synchronize(new ObjectArrayList<>());

    private final Object2ObjectMap<String, ReentrantLock> pendingBundle = Object2ObjectMaps.synchronize(new Object2ObjectArrayMap<>());

    private final IDataSource tmpSrc;
    private IDataSource bundledSrc, filesSrc;

    ProductStorage(UpdateStorage storage, ProductInfo info, IDataSource tmpSrc, IDataSource bundledSrc, IDataSource filesSrc) {
        this.storage = storage;
        this.info = info;
        this.tmpSrc = tmpSrc.resolve(info.idLong());
        updateStorageRoot(bundledSrc, filesSrc);
    }

    final void updateStorageRoot(IDataSource bundledSrc, IDataSource filesSrc) {
        this.bundledSrc = bundledSrc.resolve(info.idLong());
        this.filesSrc = filesSrc.resolve(info.idLong());
        load();
    }

    private void load() {
        versions.clear();
        instructionMap.clear();
        if (!filesSrc.exists()) {
            return;
        }
        for (IDataSource updateSrc : filesSrc.getContents()) {
            if (!updateSrc.isContainer()) {
                continue;
            }
            Version version = Version.parse(updateSrc.name());
            IDataSource instructionsSrc = updateSrc.resolve(UpdateStorage.INSTRUCTIONS_PATH);
            if (!instructionsSrc.isResource()) {
                // No instructions means invalid, we do nothing on update, makes no sense.
                storage.logger().warning("Found update '{0}' of product '{1}' without any valid instructions", version, info.name());
                continue;
            }
            try (BufferedReader reader = instructionsSrc.openReader()) {
                versions.add(version);
                String[] instr = reader.readAllLines().stream().filter(UpdateStorage.NOT_EMPTY_LINES).toArray(String[]::new);
                instructionMap.put(version, new UpdateInstructions(instr));
                storage.logger().debug("Loaded update '{0}' of product '{2}' with {1} instructions", version, instr.length, info.name());
            } catch (IOException e) {
                // Failed to read for some reason
                storage.logger().error("Failed to read instructions for update '{0}' of product '{1}'.", e, version, info.name());
                continue;
            }
        }
        Collections.sort(versions);
    }

    public Version latest() {
        if (versions.isEmpty()) {
            return null;
        }
        return versions.getLast();
    }

    public int versionCount() {
        return versions.size();
    }

    public Stream<Version> versionStream() {
        return versions.stream();
    }

    public boolean publishUpdate(Version version, UpdateInstructions instructions, byte[] zipFile, boolean allowOverwrite)
        throws IOException {
        IDataSource targetSrc = filesSrc.resolve(version.toString());
        boolean overwritten;
        if (overwritten = targetSrc.exists()) {
            if (!allowOverwrite) {
                return true;
            }
            // We are updating an existing version, not sure if we should do that but for now we'll allow it.
            targetSrc.delete();
        }
        IDataSource contentsSrc = targetSrc.resolve(UpdateStorage.CONTENTS_PATH);
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipFile), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                IDataSource fileSrc = contentsSrc.resolve(entry.getName());
                if (entry.isDirectory()) {
                    fileSrc.createAsContainer();
                    continue;
                }
                try (OutputStream outputStream = fileSrc.openWritableStream()) {
                    zip.transferTo(outputStream);
                }
            }
        }
        IDataSource instrSrc = targetSrc.resolve(UpdateStorage.INSTRUCTIONS_PATH);
        try (BufferedWriter writer = instrSrc.openWriter()) {
            for (String instr : instructions.instructions()) {
                writer.write(instr);
                writer.newLine();
            }
        }
        instructionMap.put(version, instructions);
        if (!overwritten) {
            if (versions.size() < 12) {
                versions.add(version);
                Collections.sort(versions);
            } else {
                Version target = null;
                int middle;
                int offset = 0;
                boolean higher = false;
                for (middle = (versions.size() - 1) / 2; middle > 10; middle /= 2) {
                    target = versions.get(offset + middle);
                    if (higher = version.isHigher(target)) {
                        offset += middle;
                    }
                }
                if (target == null) {
                    for (int i = 0; i < versions.size(); i++) {
                        if (version.isLower(versions.get(i))) {
                            versions.add(i, version);
                            break;
                        }
                    }
                } else if (higher) {
                    for (int i = offset + middle - 1; i >= offset; i--) {
                        if (version.isLower(versions.get(i))) {
                            versions.add(i, version);
                            break;
                        }
                    }
                } else {
                    for (int i = offset; i < middle; i++) {
                        if (version.isHigher(versions.get(i))) {
                            versions.add(i - 1, version);
                            break;
                        }
                    }
                }
            }
        }
        storage.logger().info("Product '{0}' received the update {1} with {2} instructions. (Overwritten: {3})", info.name(),
            version.toString(), instructions.instructions().length, overwritten ? "yes" : "no");
        return overwritten;
    }

    public MultiFormData buildUpdateMultiFormData(Version current, Version target) throws IOException {
        String bundlePath = UpdateStorage.BUNDLE_PATH.formatted(current, target);
        ReentrantLock lock = pendingBundle.computeIfAbsent(bundlePath, (_) -> new ReentrantLock());
        IDataSource workingSrc = bundledSrc.resolve(bundlePath);
        IDataSource instrSrc = workingSrc.resolve(UpdateStorage.INSTRUCTIONS_PATH);
        IDataSource updateSrc = workingSrc.resolve(UpdateStorage.UPDATE_ZIP_PATH);
        if (!updateSrc.exists()) {
            try {
                if (lock.tryLock(0, TimeUnit.SECONDS)) {
                    IDataSource tmpSrc = this.tmpSrc.resolve(bundlePath);
                    try {
                        applyUpdates(current, target, tmpSrc, instrSrc);
                        try (ZipOutputStream zip = new ZipOutputStream(updateSrc.openWritableStream(), StandardCharsets.UTF_8)) {
                            zip.setComment("Bundled BrickForce Aurora update (" + current.toString() + " => " + target.toString() + ")");
                            zip.setLevel(7);
                            for (IDataSource src : tmpSrc.getContents()) {
                                addToZip("", src, zip);
                            }
                        }
                    } finally {
                        tmpSrc.delete();
                    }
                } else {
                    lock.lock();
                }
            } catch (InterruptedException _) {
                if (!updateSrc.exists()) {
                    throw new IOException("Was interrupted while waiting for bundled zip to be built");
                }
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        MultiFormData data = new MultiFormData();
        data.put("zip", FileContentType.FILE_TYPE.disguise("application/zip"), IOUtil.asFile(updateSrc));
        try (BufferedReader reader = instrSrc.openReader()) {
            data.put("instructions", InstructionsContentType.UPDATE_INSTRUCTIONS,
                new UpdateInstructions(reader.lines().filter(UpdateStorage.NOT_EMPTY_LINES).toArray(String[]::new)));
        }
        return data;
    }

    private void addToZip(String path, IDataSource src, ZipOutputStream zip) throws IOException {
        if (src.isContainer()) {
            path = path + src.name() + '/';
            zip.putNextEntry(new ZipEntry(path));
            zip.closeEntry();
            for (IDataSource childSrc : src.getContents()) {
                addToZip(path, childSrc, zip);
            }
            return;
        }
        ZipEntry entry = new ZipEntry(path + src.name());
        entry.setSize(src.size());
        zip.putNextEntry(entry);
        src.transferTo(zip);
        zip.closeEntry();
    }

    private void applyUpdates(Version current, Version target, IDataSource tmpSrc, IDataSource instrSrc) throws IOException {
        Version version = current;
        int index = versions.indexOf(version);
        if (index == -1) {
            // We don't know this version.... ???????????????
            for (int i = versions.size() - 1; i >= 0; i--) {
                if (!version.isLower(versions.get(i))) {
                    index = i + 1;
                }
            }
            if (index >= versions.size()) {
                throw new IOException("Current version is too new");
            }
            if (index == -1) {
                index++;
            }
        } else {
            index++;
        }
        ObjectArrayList<String> deleteInstructions = new ObjectArrayList<>();
        ObjectArrayList<String> finalInstructions = new ObjectArrayList<>();
        UpdateActor actor = new UpdateActor(tmpSrc);
        actor.attributes.attrSet(UpdateActor.ATTR_INSTRUCTION_DELETE, deleteInstructions);
        actor.attributes.attrSet(UpdateActor.ATTR_INSTRUCTION_ANY, finalInstructions);
        IDataSource contentsSrc = tmpSrc.resolve(UpdateStorage.CONTENTS_PATH);
        for (int i = index; i < versions.size(); i++) {
            Version ver = versions.get(i);
            if (target.revision == 0 && ver.revision > 0
                || (ver.revision > 0 && (ver.major != target.major || ver.minor != target.minor || ver.patch != target.patch))) {
                continue;
            }
            if (ver.isHigher(target)) {
                break;
            }
            applyUpdate(actor, ver, contentsSrc);
        }
        try (BufferedWriter writer = instrSrc.openWriter()) {
            for (String instr : deleteInstructions) {
                writer.write(instr);
                writer.newLine();
            }
            for (String instr : finalInstructions) {
                writer.write(instr);
                writer.newLine();
            }
        }
    }

    private void applyUpdate(UpdateActor actor, Version version, IDataSource targetSrc) throws IOException {
        UpdateInstructions updateInstructions = instructionMap.get(version);
        if (updateInstructions == null) {
            return;
        }
        actor.attributes.attrSet(UpdateActor.ATTR_SOURCE, filesSrc.resolve(version.toString()).resolve(UpdateStorage.CONTENTS_PATH));
        try {
            for (String instr : updateInstructions.instructions()) {
                String[] args = CLIUtil.toArguments(instr);
                if (RestApp.LOGGER.isTracking()) {
                    RestApp.LOGGER.track("Evaluating '{0}' for update '{1}'", instr, version.toString());
                }
                String label = args[0];
                args = Arrays.copyOfRange(args, 1, args.length);
                final Triple<NodeCommand, Node, String> triple = storage.commandManager().findNode(label, args);
                if (triple == null) {
                    actor.instructions().add(instr);
                    continue;
                }
                final CommandProcess process = new CommandProcess(triple.getC(), triple.getB().getAction(), triple.getA().getInstance());
                NodeArgument argument = process.findNext(actor);
                if (argument == null) {
                    storage.commandManager().executeProcess(actor, process);
                    return;
                }
                final StringReader reader = new StringReader(CLIUtil.recombine(label, args));
                String data;
                while (argument != null) {
                    if (!reader.skipWhitespace().hasNext()) {
                        if (argument.isOptional()) {
                            process.skip(actor);
                            argument = process.findNext(actor);
                            continue;
                        }
                        throw new FailedToBuildUpdateException("Instruction argument was not provided");
                    }
                    data = reader.read();
                    try {
                        process.provide(actor, data);
                    } catch (final IllegalArgumentException exp) {
                        throw new FailedToBuildUpdateException("Couldn't parse instruction argument; " + exp.getMessage(), exp);
                    }
                    argument = process.findNext(actor);
                }
                try {
                    storage.commandManager().executeProcessWithError(actor, process);
                } catch (Throwable e) {
                    if (e instanceof IOException io) {
                        throw io;
                    }
                    throw new FailedToBuildUpdateException("Couldn't execute instruction; " + e.getMessage(), e);
                }
            }
        } finally {
            actor.attributes.attrUnset(UpdateActor.ATTR_SOURCE);
        }
    }

}
