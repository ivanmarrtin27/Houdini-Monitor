package com.houdini.monitor;

/**
 * Non-{@link javafx.application.Application} entrypoint so packaged runs work
 * when JavaFX is on the classpath (JDK 11+ otherwise requires modules).
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        HoudiniMonitorApp.main(args);
    }
}
