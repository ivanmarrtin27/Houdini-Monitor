package com.houdini.monitor.ui;

import com.houdini.monitor.i18n.Messages;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;

public final class TrayService {

    private final Stage stage;
    private final Messages messages;
    private final Consumer<Void> onExit;
    private TrayIcon trayIcon;
    private boolean installed;

    public TrayService(Stage stage, Messages messages, Consumer<Void> onExit) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.onExit = Objects.requireNonNull(onExit, "onExit");
    }

    public boolean isSupported() {
        return SystemTray.isSupported();
    }

    public boolean install() {
        if (!SystemTray.isSupported() || installed) {
            return installed;
        }
        try {
            System.setProperty("java.awt.headless", "false");
            PopupMenu menu = new PopupMenu();
            MenuItem showItem = new MenuItem(messages.get("tray.show"));
            MenuItem hideItem = new MenuItem(messages.get("tray.hide"));
            MenuItem exitItem = new MenuItem(messages.get("tray.exit"));
            showItem.addActionListener(e -> Platform.runLater(this::showStage));
            hideItem.addActionListener(e -> Platform.runLater(this::hideStage));
            exitItem.addActionListener(e -> Platform.runLater(() -> onExit.accept(null)));
            menu.add(showItem);
            menu.add(hideItem);
            menu.addSeparator();
            menu.add(exitItem);

            trayIcon = new TrayIcon(createIconImage(), messages.get("tray.tooltip"), menu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(this::showStage));
            SystemTray.getSystemTray().add(trayIcon);
            installed = true;
            return true;
        } catch (AWTException | IllegalArgumentException ex) {
            installed = false;
            return false;
        }
    }

    public void relocalize() {
        if (trayIcon == null) {
            return;
        }
        trayIcon.setToolTip(messages.get("tray.tooltip"));
        PopupMenu menu = trayIcon.getPopupMenu();
        if (menu != null && menu.getItemCount() >= 3) {
            menu.getItem(0).setLabel(messages.get("tray.show"));
            menu.getItem(1).setLabel(messages.get("tray.hide"));
            menu.getItem(menu.getItemCount() - 1).setLabel(messages.get("tray.exit"));
        }
    }

    public void showStage() {
        stage.show();
        stage.toFront();
    }

    public void hideStage() {
        stage.hide();
    }

    public void uninstall() {
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        trayIcon = null;
        installed = false;
    }

    private static Image createIconImage() {
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean border = x == 0 || y == 0 || x == size - 1 || y == size - 1;
                int rgb = border ? 0xFF14804A : 0xFF1F9D55;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }
}
