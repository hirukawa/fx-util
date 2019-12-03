package net.osdn.util.javafx.fxml;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import net.osdn.util.javafx.scene.control.ContextMenuUtil;

import java.io.IOException;
import java.io.UncheckedIOException;

public class Fxml {
    public static <T> T load(Object controller) {
        String fxmlFilename = controller.getClass().getSimpleName() + ".fxml";
        FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(fxmlFilename));
        loader.setController(controller);
        try {
            T obj = loader.load();
            fix(obj);
            return obj;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T load(Object controller, String fxmlFilename) {
        FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(fxmlFilename));
        loader.setController(controller);
        try {
            T obj = loader.load();
            fix(obj);
            return obj;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T load(String fxmlFilename) {
        Class<?> callerClass;
        try {
            callerClass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            FXMLLoader loader = new FXMLLoader(callerClass.getResource(fxmlFilename));
            T obj = loader.load();
            fix(obj);
            return obj;
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void fix(Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof MenuBar) {
            ContextMenuUtil.fix((MenuBar) obj);
        } else if (obj instanceof ContextMenu) {
            ContextMenuUtil.fix((ContextMenu) obj);
        } else if (obj instanceof Parent) {
            for (Node child : ((Parent) obj).getChildrenUnmodifiable()) {
                fix(child);
            }
        }
    }
}
