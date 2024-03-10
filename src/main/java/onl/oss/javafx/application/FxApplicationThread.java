package onl.oss.javafx.application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

public class FxApplicationThread {

	/**
	 * 呼出し側スレッドが JavaFX アプリケーション・スレッドである場合に true を返します。
	 * <p>
	 * このメソッドは {@link Platform#isFxApplicationThread()} の単純なラッパーです。</p>
	 *
	 * @return JavaFX アプリケーション・スレッドで実行する場合は true
	 */
	public static boolean isFxApplicationThread() {
		return Platform.isFxApplicationThread();
	}

	/**
	 * JavaFX アプリケーション・スレッドで指定された Runnable を将来のある時間に実行します。
	 * <p>
	 * このメソッドは {@link Platform#runLater(Runnable)} の単純なラッパーです。</p>
     *
	 * @param runnable run メソッドが JavaFX アプリケーション・スレッドで実行される Runnable
	 */
	public static void runLater(Runnable runnable) {
		Platform.runLater(runnable);
	}

	/**
	 * 指定時間経過後に、指定された Runnable を JavaFX アプリケーション・スレッドで実行します。
	 * <p>
	 * このメソッドの呼び出し自体はブロックされることなくすぐに復帰します。
	 * このメソッドは JavaFX アプリケーション・スレッドから呼び出してください。</p>
	 *
	 * @param delayMillis Runnable を実行するまでの時間（ミリ秒）
	 * @param runnable run メソッドが JavaFX アプリケーション・スレッドで実行される Runnable
	 */
	public static void runLater(long delayMillis, Runnable runnable) {
		runLater(Duration.millis(delayMillis), runnable);
	}

	/**
	 * 指定時間経過後に、指定された Runnable を JavaFX アプリケーション・スレッドで実行します。
	 * <p>
	 * このメソッドの呼び出し自体はブロックされることなくすぐに復帰します。
	 * このメソッドは JavaFX アプリケーション・スレッドから呼び出してください。</p>
	 *
	 * @param delay Runnable を実行するまでの時間。0ミリ秒以下の値を指定した場合、1ミリ秒後に実行されます。
	 * @param runnable run メソッドが JavaFX アプリケーション・スレッドで実行される Runnable
	 */
	public static void runLater(Duration delay, Runnable runnable) {
		if(delay.lessThanOrEqualTo(Duration.ZERO)) {
			delay = Duration.ONE;
		}
		new Timeline(new KeyFrame(delay, onFinished -> {
			runnable.run();
		})).play();
	}

	/**
	 * 指定された Runnable を JavaFX アプリケーション・スレッドで実行し、Runnableが完了するまで待機します。
	 * <p>
	 * このメソッドはワーカースレッドから呼び出してください。
	 * JavaFX アプリケーション・スレッドから呼び出した場合、Error がスローされます。</p>
	 *
	 * @param runnable run メソッドが JavaFX アプリケーション・スレッドで実行される Runnable
	 * @throws InterruptedException スレッドへの割り込みが発生した場合
	 * @throws InvocationTargetException Runnnable の run メソッドが例外をスローした場合
	 */
	public static void runAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {

		if(Platform.isFxApplicationThread()) {
			throw new Error("Cannot call runAndWait from the FX Application Thread");
		}

		Throwable[] throwable = new Throwable[1];
		CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(() -> {
			try {
				runnable.run();
			} catch(Throwable t) {
				throwable[0] = t;
			} finally {
				latch.countDown();
			}
		});
		latch.await();
		if(throwable[0] != null) {
			throw new InvocationTargetException(throwable[0]);
		}
	}
}
