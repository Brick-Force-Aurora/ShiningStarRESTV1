package de.brickforceaurora.restv1.integration.api;

import java.util.concurrent.atomic.AtomicBoolean;

import de.brickforceaurora.restv1.RestApp;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public abstract class IntegrationManager implements IExtension {

    protected final SnowFrame<RestApp> snowFrame = RestApp.frame();

    private final String name;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public IntegrationManager(String name) {
        this.name = name;
    }

    public final String name() {
        return name;
    }
    
    public final boolean isRunning() {
        return running.get();
    }

    public final void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        snowFrame.logger().info("Starting integration '{0}'", name);
        try {
            onStart(snowFrame);
        } catch (Throwable throwable) {
            snowFrame.logger().error("Failed to start integration '{0}'", throwable, name);
            running.set(false);
        }
    }

    public final void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        snowFrame.logger().info("Stopping integration '{0}'", name);
        try {
            onStop(snowFrame);
        } catch (Throwable throwable) {
            snowFrame.logger().error("Failed to stop integration '{0}'", throwable, name);
        }
    }

    protected abstract void onStart(SnowFrame<RestApp> snowFrame) throws Throwable;

    protected abstract void onStop(SnowFrame<RestApp> snowFrame) throws Throwable;

}
