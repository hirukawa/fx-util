package onl.oss.javafx.scene;

import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneUtil {

	/** 指定したノードのレイアウト完了後に runnable を実行します。
	 *
	 * @param parent ノード
	 * @param runnable レイアウト完了後に実行する処理
	 */
	public static void invokeAfterLayout(Parent parent, Runnable runnable) {
		if(Platform.isFxApplicationThread()) {
			invokeAfterLayoutImpl(parent, runnable);
		} else {
			Platform.runLater(() -> {
				invokeAfterLayoutImpl(parent, runnable);
			});
		}
	}

	private static void invokeAfterLayoutImpl(Parent parent, Runnable runnable) {
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

		scene.addPostLayoutPulseListener(new Runnable() {
			@Override
			public void run() {
				if(!parent.isNeedsLayout()) {
					scene.removePostLayoutPulseListener(this);
					runnable.run();
				}
			}
		});
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
