package net.osdn.util.javafx.stage;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class StageUtil {

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
			if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
				preferences.putDouble("stageX", newValue.doubleValue());
			}
		});
		stage.yProperty().addListener((observable, oldValue, newValue) -> {
			if(!stage.isMaximized() && !stage.isFullScreen() && !stage.isIconified()) {
				preferences.putDouble("stageY", newValue.doubleValue());
			}
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
}
