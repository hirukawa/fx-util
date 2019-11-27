package net.osdn.util.javafx.event;

import javafx.util.Callback;

@FunctionalInterface
public interface SilentCallback<P, R> {
    R call(P param) throws Exception;

    static <P, R> Callback<P, R> wrap(SilentCallback<P, R> callback) {
        return (param) -> {
            try {
                return callback.call(param);
            } catch(Exception e) {
                Thread.UncaughtExceptionHandler ueh = Thread.currentThread().getUncaughtExceptionHandler();
                if (ueh != null) {
                    ueh.uncaughtException(Thread.currentThread(), e);
                    return null;
                } else {
                    throw new SilentWrappedException(e);
                }
            }
        };
    }
}
