package net.osdn.util.javafx.event;

@FunctionalInterface
public interface SilentRunnable {
	void run() throws Exception;

	static Runnable wrap(SilentRunnable runnable) {
		return () -> {
			try {
				runnable.run();
			} catch(Exception e) {
				Thread.UncaughtExceptionHandler ueh = Thread.currentThread().getUncaughtExceptionHandler();
				if (ueh != null) {
					ueh.uncaughtException(Thread.currentThread(), e);
				} else {
					rethrow(e);
				}
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
