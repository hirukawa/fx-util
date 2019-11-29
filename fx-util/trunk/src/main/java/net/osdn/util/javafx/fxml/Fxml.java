package net.osdn.util.javafx.fxml;

import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.UncheckedIOException;

public class Fxml {
    public static <T> T load(Object controller) {
        String fxmlFilename = controller.getClass().getSimpleName() + ".fxml";
        FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(fxmlFilename));
        loader.setController(controller);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
