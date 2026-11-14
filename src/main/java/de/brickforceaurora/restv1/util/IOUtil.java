package de.brickforceaurora.restv1.util;

import java.io.File;
import java.util.Objects;

import me.lauriichan.snowframe.resource.source.FileDataSource;
import me.lauriichan.snowframe.resource.source.IDataSource;
import me.lauriichan.snowframe.resource.source.PathDataSource;

public final class IOUtil {

    private IOUtil() {
        throw new UnsupportedOperationException();
    }

    public static File asFile(final IDataSource source) {
        Objects.requireNonNull(source, "IDataSource can't be null");
        if (source instanceof final FileDataSource fileSource) {
            return fileSource.getSource();
        }
        if (source instanceof final PathDataSource pathSource) {
            return pathSource.getSource().toFile();
        }
        throw new IllegalArgumentException("Can't convert IDataSource of type '%s' to Path".formatted(source.getClass().getSimpleName()));
    }

}
