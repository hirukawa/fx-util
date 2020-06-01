package net.osdn.util.javafx.concurrent;

import javafx.concurrent.Task;
import net.osdn.util.javafx.event.SilentRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Async<V> implements AsyncRunnable, AsyncCallable<V> {

	/**
	 * タスク完了時の状態
	 *
	 */
	public enum State {
		/**
		 * タスクがキャンセルされたことを示します。
		 */
		CANCELLED,
		/**
		 * タスクが正常に終了したことを示します。
		 */
		SUCCEEDED,
		/**
		 * 通常は予期しない条件が発生したことによって、タスクが失敗したことを示します。
		 */
		FAILED
	}

	private static ThreadFactory defaultThreadFactory;

	/**
	 * 新規スレッドの作成に使用するデフォルトのスレッド・ファクトリーを返します。
	 *
	 * @return スレッド・ファクトリー
	 */
	public static ThreadFactory getDefaultThreadFactory() {
		return defaultThreadFactory;
	}

	/**
	 * 新規スレッドの作成に使用するデフォルトのスレッド・ファクトリーを設定します。
	 * スレッド・ファクトリーを設定するとエグゼキューターは再初期化されます。
	 *
	 * @param factory スレッド・ファクトリー
	 */
	public static void setDefaultThreadFactory(ThreadFactory factory) {
		defaultThreadFactory = factory;
		executor = null;
	}

	private static Executor executor;

	/**
	 * タスクを実行するエグゼキューターを返します。
	 * エグゼキューターが明示的に設定されていない場合、
	 * 既定のスレッド・ファクトリーを使ってスレッドをプールして再利用するエグゼキューターが新たに作成されます。
	 * 既定のスレッド・ファクトリーが設定されていない場合、デーモン・スレッドを作成するファクトリーが使われます。
	 *
	 * @return エグゼキューター
	 */
	public static Executor getExecutor() {
		if(executor == null) {
			ThreadFactory factory = getDefaultThreadFactory();
			if(factory == null) {
				factory = new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread thread = new Thread(r);
						thread.setDaemon(true);
						return thread;
					}
				};
			}
			executor = Executors.newCachedThreadPool(factory);
		}
		return executor;
	}

	/**
	 * タスクを実行するエグゼキューターを設定します。
	 * エグゼキューターが明示的に設定された場合、既定のスレッド・ファクトリーは使われません。
	 *
	 * @param executor エグゼキューター
	 */
	public static void setExecutor(Executor executor) {
		Async.executor = executor;
	}

	/**
	 * 指定したRunnableを非同期で実行します。
	 *
	 * @param runnable 非同期で実行する処理
	 * @return メソッドチェーンでハンドラーを設定できます。
	 */
	public static AsyncRunnable execute(SilentRunnable runnable) {
		Async<Void> async = new Async<Void>(() -> {
			runnable.run();
			return null;
		});
		getExecutor().execute(async.task);
		return async;
	}

	/**
	 * 指定したCallableを非同期で実行します。
	 * 処理は値を返すことができます。
	 *
	 * @param callable 非同期で実行する処理
	 * @param <V> 処理の戻り値の型
	 * @return メソッドチェーンでハンドラーを設定できます。
	 */
	public static <V> AsyncCallable<V> execute(Callable<V> callable) {
		Async<V> async = new Async<V>(callable);
		getExecutor().execute(async.task);
		return async;
	}

	private Task<V> task;
	private Cancel cancel;
	private AsyncRunnable.Success runnableSuccess;
	private AsyncCallable.Success<V> callableSuccess;
	private Failure failure;
	private Complete complete;

	private Async(Callable<V> callable) {
		task = new Task<V>() {
			@Override
			protected V call() throws Exception {
				return callable.call();
			}

			@Override
			protected void cancelled() {
				Async.this.cancelled();
			}

			@Override
			protected void succeeded() {
				Async.this.succeeded();
			}

			@Override
			protected void failed() {
				Async.this.failed();
			}
		};
	}

	protected void cancelled() {
		Exception exception = null;
		try {
			if (cancel != null) {
				cancel.onCancelled();
			}
		} catch(Exception e) {
			exception = e;
		} finally {
			if(complete != null) {
				try {
					complete.onCompleted(State.CANCELLED);
				} catch(Exception e) {
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(exception != null) {
				rethrow(exception);
			}
		}
	}

	protected void succeeded() {
		Exception exception = null;
		try {
			if (runnableSuccess != null) {
				runnableSuccess.onSucceeded();
			} else if (callableSuccess != null) {
				callableSuccess.onSucceeded(task.getValue());
			}
		} catch(Exception e) {
			exception = e;
		} finally {
			if(complete != null) {
				try {
					complete.onCompleted(State.SUCCEEDED);
				} catch(Exception e) {
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(exception != null) {
				rethrow(exception);
			}
		}
	}

	protected void failed() {
		Throwable t = task.getException();
		if(t instanceof Error) {
			throw (Error)t;
		}
		Exception exception = null;
		try {
			if (failure != null) {
				failure.onFailed((Exception)t);
			} else {
				exception = (Exception)t;
			}
		} catch(Exception e) {
			exception = e;
		} finally {
			if(complete != null) {
				try {
					complete.onCompleted(State.FAILED);
				} catch(Exception e) {
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(exception != null) {
				rethrow(exception);
			}
		}
	}

	/**
	 * タスクを返します。
	 *
	 * @return タスク
	 */
	public Task<V> getTask() {
		return this.task;
	}

	/**
	 * タスクがキャンセルされたときに実行されるハンドラーを設定します。
	 * ハンドラーはJavaFXアプリケーションスレッドで呼び出されます。
	 *
	 * @param callback タスクがキャンセルされたときに呼び出されるハンドラー
	 * @return メソッドチェーンで他のハンドラーの設定を続けられます。
	 */
	public Async<V> onCancelled(Cancel callback) {
		this.cancel = callback;
		return this;
	}

	/**
	 * タスクが正常に終了したときに呼び出されるハンドラーを設定します。
	 * ハンドラーはJavaFXアプリケーションスレッドで呼び出されます。
	 *
	 * @param callback タスクが正常に終了したときに呼び出されるハンドラー
	 * @return メソッドチェーンで他のハンドラーの設定を続けられます。
	 */
	public Async<V> onSucceeded(AsyncRunnable.Success callback) {
		this.callableSuccess = null;
		this.runnableSuccess = callback;
		return this;
	}

	/**
	 * タスクが正常に終了したときに呼び出されるハンドラーを設定します。
	 * ハンドラーはJavaFXアプリケーションスレッドで呼び出されます。
	 *
	 * @param callback タスクが正常に終了したときに呼び出されるハンドラー
	 * @return メソッドチェーンで他のハンドラーの設定を続けられます。
	 */
	public Async<V> onSucceeded(AsyncCallable.Success<V> callback) {
		this.runnableSuccess = null;
		this.callableSuccess = callback;
		return this;
	}

	/**
	 * 例外がスローされてタスクが失敗したときに呼び出されるハンドラーを設定します。
	 * ハンドラーはJavaFXアプリケーションスレッドで呼び出されます。
	 *
	 * @param callback タスクが失敗したときに呼び出されるハンドラー
	 * @return メソッドチェーンで他のハンドラーの設定を続けられます。
	 */
	public Async<V> onFailed(Failure callback) {
		this.failure = callback;
		return this;
	}

	/**
	 * タスクが完了したときに呼び出されるハンドラーを設定します。
	 * ハンドラーはJavaFXアプリケーションスレッドで呼び出されます。
	 *
	 * タスクがキャンセルされた場合、タスクが正常に終了した場合、タスクが失敗した場合、
	 * いずれの場合でも、最期にこのハンドラーが呼び出されます。
	 *
	 * @param callback タスクが完了したときに呼び出されるハンドラー
	 * @return メソッドチェーンで他のハンドラーの設定を続けられます。
	 */
	public Async<V> onCompleted(Complete callback) {
		this.complete = callback;
		return this;
	}

	@FunctionalInterface
	public interface Cancel {
		void onCancelled() throws Exception;
	}

	@FunctionalInterface
	public interface Failure {
		void onFailed(Exception exception) throws Exception;
	}

	@FunctionalInterface
	public interface Complete {
		void onCompleted(State state) throws Exception;
	}

	static void rethrow(Throwable throwable) {
		rethrow0(throwable);
	}

	@SuppressWarnings("unchecked")
	static <T extends Throwable> void rethrow0(Throwable throwable) throws T {
		throw (T)throwable;
	}
}
