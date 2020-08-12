package net.osdn.util.javafx.event;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

@FunctionalInterface
public interface SilentInvalidationListener {
	void invalidated(Observable observable) throws Exception;

	static InvalidationListener wrap(SilentInvalidationListener listener) {
		return (observable) -> {
			try {
				listener.invalidated(observable);
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