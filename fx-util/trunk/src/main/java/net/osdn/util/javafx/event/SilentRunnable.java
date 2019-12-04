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
					throw new SilentWrappedException(e);
				}
			}
		};
	}
}
