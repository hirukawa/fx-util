package net.osdn.util.javafx;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Unchecked {

	public interface Runnable {
		void run() throws Exception;
	}

	/** 指定したタスクを実行する Runnable を返します。
	 * タスクは try ～ catch(Throwable) 内で実行されるため、チェック例外をスローするコードをそのまま含むことができます。
	 * タスク実行でキャッチされない例外がスローされると、スレッドのキャッチされない例外ハンドラ（UncaughtExceptionHandler）に転送します。
	 *
	 * @param runnable 実行可能なタスク
	 * @return 実行可能なタスクをラップした Runnable
	 */
	public static java.lang.Runnable runnable(Unchecked.Runnable runnable) {
		return new java.lang.Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch(Throwable exception) {
					Thread thread = Thread.currentThread();
					Thread.UncaughtExceptionHandler ueh = thread.getUncaughtExceptionHandler();
					if(ueh != null) {
						ueh.uncaughtException(thread, exception);
					} else {
						rethrow(exception);
					}
				}
			}
		};
	}

	/** 指定したタスクを実行します。
	 * タスクは try ～ catch(Throwable) 内で実行されるため、チェック例外をスローするコードをそのまま含むことができます。
	 * タスク実行でキャッチされない例外がスローされると、スレッドのキャッチされない例外ハンドラ（UncaughtExceptionHandler）に転送します。
	 *
	 * @param runnable 実行可能なタスク
	 */
	public static void execute(Unchecked.Runnable runnable) {
		try {
			runnable.run();
		} catch(Throwable exception) {
			Thread thread = Thread.currentThread();
			Thread.UncaughtExceptionHandler ueh = thread.getUncaughtExceptionHandler();
			if(ueh != null) {
				ueh.uncaughtException(thread, exception);
			} else {
				rethrow(exception);
			}
		}
	}

	/** 戻り値を持つタスクを実行します。
	 * タスクは try ～ catch(Throwable) 内で実行されるため、チェック例外をスローするコードをそのまま含むことができます。
	 * タスク実行でキャッチされない例外がスローされると、スレッドのキャッチされない例外ハンドラ（UncaughtExceptionHandler）に転送します。
	 *
	 * @param callable 戻り値を持つタスク
	 * @return タスクの戻り値
	 * @param <T> タスクの戻り値の型
	 */
	public static <T> T execute(Callable<T> callable) {
		try {
			return callable.call();
		} catch(Throwable exception) {
			Thread thread = Thread.currentThread();
			Thread.UncaughtExceptionHandler ueh = thread.getUncaughtExceptionHandler();
			if(ueh != null) {
				ueh.uncaughtException(thread, exception);
			} else {
				rethrow(exception);
			}
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void rethrow(Throwable throwable) throws T {
		throw (T)throwable;
	}
}
