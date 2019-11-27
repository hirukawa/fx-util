package net.osdn.util.javafx.event;

@SuppressWarnings("serial")
public class SilentWrappedException extends RuntimeException {
    public SilentWrappedException(Exception cause) {
        super(cause);
    }
}
