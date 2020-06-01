package net.osdn.util.javafx.event;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface SilentCallable<V> {
	V call() throws Exception;

	static <V> Callable<V> wrap(SilentCallable<V> callable) {
		return () -> {
			try {
				return callable.call();
			} catch(Exception e) {
				Thread.UncaughtExceptionHandler ueh = Thread.currentThread().getUncaughtExceptionHandler();
				if (ueh != null) {
					ueh.uncaughtException(Thread.currentThread(), e);
				} else {
					rethrow(e);
				}
				return null;
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
