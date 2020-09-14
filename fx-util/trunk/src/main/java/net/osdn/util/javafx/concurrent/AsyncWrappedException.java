package net.osdn.util.javafx.concurrent;

@SuppressWarnings("serial")
public class AsyncWrappedException extends RuntimeException {
    public AsyncWrappedException(Throwable cause) {
        super(cause);
    }
}
