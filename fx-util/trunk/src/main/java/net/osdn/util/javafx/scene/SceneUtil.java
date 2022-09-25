package net.osdn.util.javafx.scene;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneUtil {

	/** 指定したノードのレイアウト完了後に runnable を実行します。
	 *
	 * @param parent ノード
	 * @param runnable レイアウト完了後に実行する処理
	 */
	public static void invokeAfterLayout(Parent parent, Runnable runnable) {
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
					Platform.runLater(runnable);
				}
			}
		});
		Platform.requestNextPulse();
	}
}
