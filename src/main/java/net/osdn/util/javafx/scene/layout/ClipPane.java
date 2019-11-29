package net.osdn.util.javafx.scene.layout;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class ClipPane extends Pane {

	public ClipPane() {
		setClip(new Rectangle());
		layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
			Node clip = getClip();
			if(clip instanceof Rectangle) {
				((Rectangle)clip).setWidth(newValue.getWidth());
				((Rectangle)clip).setHeight(newValue.getHeight());
			}
		});
	}
}
