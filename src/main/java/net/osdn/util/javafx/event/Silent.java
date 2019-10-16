package net.osdn.util.javafx.event;

import javafx.event.Event;
import javafx.event.EventHandler;

@FunctionalInterface
public interface Silent<T extends Event> {
	void handle(T event) throws Exception;

	static <T extends Event> EventHandler<T> wrap(Silent<T> handler) {
		return event -> {
			try {
				handler.handle(event);
			} catch (Exception e) {
				Thread.UncaughtExceptionHandler ueh = Thread.currentThread().getUncaughtExceptionHandler();
				if(ueh != null) {
					ueh.uncaughtException(Thread.currentThread(), e);
					return;
				}
				throw new Silent.WrappedException(e);
			}
		};
	}

	class WrappedException extends RuntimeException {
		public WrappedException(Exception cause) {
			super(cause);
		}
	}
}
