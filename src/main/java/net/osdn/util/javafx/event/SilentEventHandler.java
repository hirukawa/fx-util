package net.osdn.util.javafx.event;

import javafx.event.Event;
import javafx.event.EventHandler;

@FunctionalInterface
public interface SilentEventHandler<T extends Event> {
	void handle(T event) throws Exception;

	static <T extends Event> EventHandler<T> wrap(SilentEventHandler<T> handler) {
		return event -> {
			try {
				handler.handle(event);
			} catch (Exception e) {
				Thread.UncaughtExceptionHandler ueh = Thread.currentThread().getUncaughtExceptionHandler();
				if(ueh != null) {
					ueh.uncaughtException(Thread.currentThread(), e);
				} else {
					rethrow(e);
				}
				return;
			}
		};
	}

	static void rethrow(Throwable throwable) {
		rethrow0(throwable);
	}

	@SuppressWarnings("unchecked")
	static <T extends Throwable> void rethrow0(Throwable throwable) throws T {
		throw (T)throwable;
	}
}
