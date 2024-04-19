package onl.oss.javafx.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;

public class SceneUtil {

	/** 指定したノードのレイアウト完了後に runnable を実行します。
	 * 既定の時間（500ミリ秒）が経過してもレイアウトが完了していない場合も runnable を実行します。
	 *
	 * @param parent ノード
	 * @param runnable レイアウト完了後に実行する処理
	 */
	public static void invokeAfterLayout(Parent parent, Runnable runnable) {
		invokeAfterLayout(parent, runnable, null);
	}

	/** 指定したノードのレイアウト完了後に runnable を実行します。
	 * 指定した時間が経過してもレイアウトが完了していない場合も runnable を実行します。
	 *
	 * @param parent ノード
	 * @param runnable レイアウト完了後に実行する処理
	 * @param timeout タイムアウト
	 */
	public static void invokeAfterLayout(Parent parent, Runnable runnable, Duration timeout) {
		if(Platform.isFxApplicationThread()) {
			invokeAfterLayoutImpl(parent, runnable, timeout);
		} else {
			Platform.runLater(() -> {
				invokeAfterLayoutImpl(parent, runnable, timeout);
			});
		}
	}

	private static void invokeAfterLayoutImpl(Parent parent, Runnable runnable, Duration timeout) {
		if(parent == null) {
			throw new NullPointerException();
		}

		if(runnable == null) {
			throw new NullPointerException();
		}

		Scene scene = parent.getScene();
		if(scene == null) {
			throw new NullPointerException();
		}

		final boolean[] isComplated = new boolean[1];

		final Runnable preLayoutPulseListener = new Runnable() {
			@Override
			public void run() {
				scene.removePreLayoutPulseListener(this);

				if (!isComplated[0]) {
					isComplated[0] = true;
					runnable.run();
				}
			}
		};

		final Runnable postLayoutPulseListener = new Runnable() {
			@Override
			public void run() {
				if(!parent.isNeedsLayout()) {
					scene.removePostLayoutPulseListener(this);
					scene.addPreLayoutPulseListener(preLayoutPulseListener);
					Platform.requestNextPulse();
				}
			}
		};

		if (timeout == null) {
			timeout = Duration.millis(500);
		}

		new Timeline(new KeyFrame(timeout, event -> {
			if (!isComplated[0]) {
				isComplated[0] = true;
				scene.removePostLayoutPulseListener(postLayoutPulseListener);
				scene.removePreLayoutPulseListener(preLayoutPulseListener);
				runnable.run();
			}
		})).play();

		scene.addPostLayoutPulseListener(postLayoutPulseListener);
		Platform.requestNextPulse();
	}

	public static void invokeAfterAnimation(Animation animation, Runnable runnable) {
		if(Platform.isFxApplicationThread()) {
			invokeAfterAnimationImpl(animation, runnable);
		} else {
			Platform.runLater(() -> {
				invokeAfterAnimationImpl(animation, runnable);
			});
		}
	}

	private static void invokeAfterAnimationImpl(Animation animation, Runnable runnable) {
		if(animation == null) {
			throw new NullPointerException();
		}

		if(runnable == null) {
			throw new NullPointerException();
		}

		if(animation.getStatus() != Animation.Status.RUNNING) {
			runnable.run();
		} else {
			ReadOnlyObjectProperty<Animation.Status> status = animation.statusProperty();
			status.addListener(new ChangeListener<Animation.Status>() {
				@Override
				public void changed(ObservableValue<? extends Animation.Status> observable, Animation.Status oldValue, Animation.Status newValue) {
					if(newValue != Animation.Status.RUNNING) {
						status.removeListener(this);
						runnable.run();
					}
				}
			});
		}
	}
}
