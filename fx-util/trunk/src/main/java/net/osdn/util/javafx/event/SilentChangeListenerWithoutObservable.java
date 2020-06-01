package net.osdn.util.javafx.event;

import javafx.beans.value.ChangeListener;

@FunctionalInterface
public interface SilentChangeListenerWithoutObservable<T> {
	void changed(T oldValue, T newValue) throws Exception;

	static <T> ChangeListener<T> wrap(SilentChangeListenerWithoutObservable<T> listener) {
		return (observable, oldValue, newValue) -> {
			try {
				listener.changed(oldValue, newValue);
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
