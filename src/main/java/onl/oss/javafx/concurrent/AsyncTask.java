package onl.oss.javafx.concurrent;

import javafx.concurrent.Task;
import onl.oss.javafx.Unchecked;

import java.util.concurrent.Callable;

public class AsyncTask<V> extends Task<V> {

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

	private Callable<V> callable;
	private Cancel cancel;
	private Success<V> success;
	private Fail fail;
	private Finish finish;

	public static AsyncTask<Void> create(Unchecked.Runnable runnable) {
		return new AsyncTask<Void>(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				runnable.run();
				return null;
			}
		});
	}

	public static <V> AsyncTask<V> create(Callable<V> callable) {
		return new AsyncTask<V>(callable);
	}

	public AsyncTask(Callable<V> callable) {
		this.callable = callable;
	}

	public AsyncTask<V> onSucceeded(Success<V> callback) {
		this.success = callback;
		return this;
	}

	public AsyncTask<V> onCancelled(Cancel callback) {
		this.cancel = callback;
		return this;
	}

	public AsyncTask<V> onFailed(Fail callback) {
		this.fail = callback;
		return this;
	}

	public AsyncTask<V> onFinished(Finish callback) {
		this.finish = callback;
		return this;
	}

	@Override
	protected V call() throws Exception {
		return callable.call();
	}

	@Override
	protected void succeeded() {
		Throwable exception = null;
		try {
			if (success != null) {
				success.onSucceeded(getValue());
			}
		} catch(Throwable e) {
			exception = e;
		} finally {
			if(finish != null) {
				try {
					finish.onFinished(State.SUCCEEDED);
				} catch(Throwable e) {
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(exception != null) {
				Thread thread = Thread.currentThread();
				Thread.UncaughtExceptionHandler ueh = thread.getUncaughtExceptionHandler();
				if(ueh != null) {
					ueh.uncaughtException(thread, exception);
				} else {
					rethrow(exception);
				}
			}
		}
	}

	@Override
	protected void cancelled() {
		Throwable exception = null;
		try {
			if (cancel != null) {
				cancel.onCancelled();
			}
		} catch(Throwable e) {
			exception = e;
		} finally {
			if(finish != null) {
				try {
					finish.onFinished(State.CANCELLED);
				} catch(Throwable e) {
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(exception != null) {
				Thread thread = Thread.currentThread();
				Thread.UncaughtExceptionHandler ueh = thread.getUncaughtExceptionHandler();
				if(ueh != null) {
					ueh.uncaughtException(thread, exception);
				} else {
					rethrow(exception);
				}
			}
		}
	}

	@Override
	protected void failed() {
		Throwable exception = null;
		try {
			if (fail != null) {
				fail.onFailed(getException());
			} else {
				exception = getException();
			}
		} catch(Throwable e) {
			exception = e;
		} finally {
			if(finish != null) {
				try {
					finish.onFinished(State.FAILED);
				} catch(Throwable e) {
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(exception != null) {
				Thread thread = Thread.currentThread();
				Thread.UncaughtExceptionHandler ueh = thread.getUncaughtExceptionHandler();
				if(ueh != null) {
					ueh.uncaughtException(thread, exception);
				} else {
					rethrow(exception);
				}
			}
		}
	}

	@FunctionalInterface
	public interface Success<V> {
		void onSucceeded(V result) throws Exception;
	}

	@FunctionalInterface
	public interface Cancel {
		void onCancelled() throws Exception;
	}

	@FunctionalInterface
	public interface Fail {
		void onFailed(Throwable exception) throws Exception;
	}

	@FunctionalInterface
	public interface Finish {
		void onFinished(State state) throws Exception;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void rethrow(Throwable throwable) throws T {
		throw (T)throwable;
	}
}
