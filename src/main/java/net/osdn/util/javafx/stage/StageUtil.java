package net.osdn.util.javafx.stage;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Properties;
import java.util.prefs.Preferences;

public class StageUtil {

	private static boolean getBoolean(Properties properties, String key, boolean defaultValue) {
		String value = properties.getProperty(key);
		try {
			if(value != null) {
				return Boolean.parseBoolean(value);
			}
		} catch(Exception ignore) {}
		return defaultValue;
	}

	private static void putBoolean(Properties properties, String key, boolean value) {
		properties.put(key, Boolean.toString(value));
	}

	private static double getDouble(Properties properties, String key, double defaultValue) {
		String value = properties.getProperty(key);
		try {
			if(value != null) {
				return Double.parseDouble(value);
			}
		} catch(Exception ignore) {}
		return defaultValue;
	}

	private static void putDouble(Properties properties, String key, double value) {
		properties.put(key, Double.toString(value));
	}

	/**
	 * ステージの位置とサイズを復元可能にします。
	 *
	 * @param stage ステージ
	 * @param properties プロパティ―
	 */
	public static void setRestorable(Stage stage, Properties properties) {
		double x = getDouble(properties, "stageX", stage.getX());
		double y = getDouble(properties, "stageY", stage.getY());
		double w = getDouble(properties,"stageWidth", stage.getWidth());
		double h = getDouble(properties,"stageHeight", stage.getHeight());
		boolean maximized = getBoolean(properties, "stageMaximized", false);
		Rectangle2D workarea = Screen.getPrimary().getVisualBounds();
		if(x + w > workarea.getMaxX()) {
			x = workarea.getMaxX() - w;
		}
		if(x < workarea.getMinX()) {
			x = workarea.getMinX();
		}
		if(x + w > workarea.getMaxX()) {
			w = workarea.getMaxX() - x;
		}
		if(y + h > workarea.getMaxY()) {
			y = workarea.getMaxY() - h;
		}
		if(y < workarea.getMinY()) {
			y = workarea.getMinY();
		}
		if(y + h > workarea.getMaxY()) {
			h = workarea.getMaxY() - y;
		}
		stage.setX(x);
		stage.setY(y);
		stage.setWidth(w);
		stage.setHeight(h);
		stage.setMaximized(maximized);

		stage.xProperty().addListener((observable, oldValue, newValue) -> {
			// 最大化操作をしたときに、stage.isMaximized が true になる前に x が左上座標に変更されてしまいます。
			// 最大化操作時に x 座標が保存されないように Platform.runLater で判定を遅延させます。
			// Platform.runLater で指定したタスクは最大化が完了した後に呼び出されるため、stage.isMaximized が有意な値になっています。
			// これは x, y に必要な対処です。width, height の変更は isMaximized の変更より後でおこなわれるため、
			// Platform.runLater で遅延させなくても正しく最大化状態を判定できます。
			Platform.runLater(() -> {
				if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
					putDouble(properties,"stageX", newValue.doubleValue());
				}
			});
		});
		stage.yProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
					putDouble(properties, "stageY", newValue.doubleValue());
				}
			});
		});
		stage.widthProperty().addListener((observable, oldValue, newValue) -> {
			if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
				putDouble(properties, "stageWidth", newValue.doubleValue());
			}
		});
		stage.heightProperty().addListener((observable, oldValue, newValue) -> {
			if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
				putDouble(properties, "stageHeight", newValue.doubleValue());
			}
		});
		stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
			putBoolean(properties,"stageMaximized", newValue.booleanValue());
		});
	}

	/**
	 * ステージの位置とサイズを復元可能にします。
	 *
	 * @param stage ステージ
	 * @param preferences プリファレンス
	 */
	public static void setRestorable(Stage stage, Preferences preferences) {
		double x = preferences.getDouble("stageX", stage.getX());
		double y = preferences.getDouble("stageY", stage.getY());
		double w = preferences.getDouble("stageWidth", stage.getWidth());
		double h = preferences.getDouble("stageHeight", stage.getHeight());
		boolean maximized = preferences.getBoolean("stageMaximized", false);
		Rectangle2D workarea = Screen.getPrimary().getVisualBounds();
		if(x + w > workarea.getMaxX()) {
			x = workarea.getMaxX() - w;
		}
		if(x < workarea.getMinX()) {
			x = workarea.getMinX();
		}
		if(x + w > workarea.getMaxX()) {
			w = workarea.getMaxX() - x;
		}
		if(y + h > workarea.getMaxY()) {
			y = workarea.getMaxY() - h;
		}
		if(y < workarea.getMinY()) {
			y = workarea.getMinY();
		}
		if(y + h > workarea.getMaxY()) {
			h = workarea.getMaxY() - y;
		}
		stage.setX(x);
		stage.setY(y);
		stage.setWidth(w);
		stage.setHeight(h);
		stage.setMaximized(maximized);

		stage.xProperty().addListener((observable, oldValue, newValue) -> {
			// 最大化操作をしたときに、stage.isMaximized が true になる前に x が左上座標に変更されてしまいます。
			// 最大化操作時に x 座標が保存されないように Platform.runLater で判定を遅延させます。
			// Platform.runLater で指定したタスクは最大化が完了した後に呼び出されるため、stage.isMaximized が有意な値になっています。
			// これは x, y に必要な対処です。width, height の変更は isMaximized の変更より後でおこなわれるため、
			// Platform.runLater で遅延させなくても正しく最大化状態を判定できます。
			Platform.runLater(() -> {
				if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
					preferences.putDouble("stageX", newValue.doubleValue());
				}
			});
		});
		stage.yProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
					preferences.putDouble("stageY", newValue.doubleValue());
				}
			});
		});
		stage.widthProperty().addListener((observable, oldValue, newValue) -> {
			if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
				preferences.putDouble("stageWidth", newValue.doubleValue());
			}
		});
		stage.heightProperty().addListener((observable, oldValue, newValue) -> {
			if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
				preferences.putDouble("stageHeight", newValue.doubleValue());
			}
		});
		stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
			preferences.putBoolean("stageMaximized", newValue.booleanValue());
		});
	}

	/** 指定サイズのSceneが収まるようにStageの最小サイズを設定します。
	 *
	 * StageのsetMinWidth, setMinHeightで指定した場合、Sceneを内包するのに適切なサイズになりません。
	 * たとえば、Windows 10 で Stage.setMinWidth(1360) と設定した場合、ウィンドウの幅は 1346 になってしまいます。
	 * これはウィンドウボーダーを考慮しても小さくなりすぎています。
	 *
	 * この問題を回避するために、内包する Scene にサイズを設定した後、Stage の sizeToScene で Stage のサイズを Scene に合わせます。
	 * この Stage のサイズを最小サイズとして設定することで、Scene を収めるのに最適なサイズが設定されます。
	 *
	 * @param stage ステージ
	 * @param width ステージ内側の最小の幅
	 * @param height ステージ内側の最小の高さ
	 */
	public static void setMinSize(Stage stage, int width, int height) {
		Region r = new Region();
		r.setPrefSize(width, height);
		r.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		r.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		Scene scene = new Scene(r);
		Stage dummy = new Stage(stage.getStyle());
		dummy.setScene(scene);
		dummy.setOpacity(0.0);
		dummy.show();
		dummy.sizeToScene();
		double minWidth = dummy.getWidth();
		double minHeight = dummy.getHeight();
		dummy.hide();
		stage.setMinWidth(minWidth);
		stage.setMinHeight(minHeight);
	}
}
