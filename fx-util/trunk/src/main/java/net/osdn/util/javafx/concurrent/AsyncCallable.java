package net.osdn.util.javafx.concurrent;

public interface AsyncCallable<V> {
	/**
	 * タスクがキャンセルされたときに実行されるハンドラーを設定します。
	 * ハンドラーはJavaFXアプリケーションスレッドで呼び出されます。
	 *
	 * @param callback タスクがキャンセルされたときに呼び出されるハンドラー
	 * @return メソッドチェーンで他のハンドラーの設定を続けられます。
	 */
	AsyncCallable<V> onCancelled(Async.Cancel callback);

	/**
	 * タスクが正常に終了したときに呼び出されるハンドラーを設定します。
	 * ハンドラーはJavaFXアプリケーションスレッドで呼び出されます。
	 *
	 * @param callback タスクが正常に終了したときに呼び出されるハンドラー
	 * @return メソッドチェーンで他のハンドラーの設定を続けられます。
	 */
	AsyncCallable<V> onSucceeded(Success<V> callback);

	/**
	 * 例外がスローされてタスクが失敗したときに呼び出されるハンドラーを設定します。
	 * ハンドラーはJavaFXアプリケーションスレッドで呼び出されます。
	 *
	 * @param callback タスクが失敗したときに呼び出されるハンドラー
	 * @return メソッドチェーンで他のハンドラーの設定を続けられます。
	 */
	AsyncCallable<V> onFailed(Async.Failure callback);

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
	AsyncCallable<V> onCompleted(Async.Complete callback);

	@FunctionalInterface
	public interface Success<T> {
		void onSucceeded(T result) throws Exception;
	}
}
