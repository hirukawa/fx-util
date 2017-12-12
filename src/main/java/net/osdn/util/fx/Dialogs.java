package net.osdn.util.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Dialogs {
	
	public static final Image IMAGE_INFORMATION = new Image(Dialogs.class.getResourceAsStream("/img/dialog-information-36px.png"));
	public static final Image IMAGE_CONFIRMATION = new Image(Dialogs.class.getResourceAsStream("/img/dialog-confirmation-36px.png"));
	public static final Image IMAGE_WARNING = new Image(Dialogs.class.getResourceAsStream("/img/dialog-warning-36px.png"));
	public static final Image IMAGE_ERROR = new Image(Dialogs.class.getResourceAsStream("/img/dialog-error-36px.png"));
	public static final Image IMAGE_SUCCESS = new Image(Dialogs.class.getResourceAsStream("/img/dialog-success-36px.png"));
	
	public static String BUTTON_CANCEL_TEXT = "キャンセル";
	
	public static ButtonType showInformation(String message) {
		return show(AlertType.INFORMATION, null, IMAGE_INFORMATION, null, message, null);
	}
	
	public static ButtonType showInformation(String title, String message) {
		return show(AlertType.INFORMATION, null, IMAGE_INFORMATION, title, message, null);
	}
	
	public static ButtonType showInformation(Window owner, String message) {
		return show(AlertType.INFORMATION, owner, IMAGE_INFORMATION, null, message, null);
	}
	
	public static ButtonType showInformation(Window owner, String title, String message) {
		return show(AlertType.INFORMATION, owner, IMAGE_INFORMATION, title, message, null);
	}
	
	public static ButtonType showConfirmation(String message) {
		Map<ButtonType, String> captions = new HashMap<ButtonType, String>();
		captions.put(ButtonType.CANCEL, BUTTON_CANCEL_TEXT);
		return show(AlertType.CONFIRMATION, null, IMAGE_CONFIRMATION, null, message, captions);
	}
	
	public static ButtonType showConfirmation(String title, String message) {
		Map<ButtonType, String> captions = new HashMap<ButtonType, String>();
		captions.put(ButtonType.CANCEL, BUTTON_CANCEL_TEXT);
		return show(AlertType.CONFIRMATION, null, IMAGE_CONFIRMATION, title, message, captions);
	}
	
	public static ButtonType showConfirmation(Window owner, String message) {
		Map<ButtonType, String> captions = new HashMap<ButtonType, String>();
		captions.put(ButtonType.CANCEL, BUTTON_CANCEL_TEXT);
		return show(AlertType.CONFIRMATION, owner, IMAGE_CONFIRMATION, null, message, captions);
	}
	
	public static ButtonType showConfirmation(Window owner, String title, String message) {
		Map<ButtonType, String> captions = new HashMap<ButtonType, String>();
		captions.put(ButtonType.CANCEL, BUTTON_CANCEL_TEXT);
		return show(AlertType.CONFIRMATION, owner, IMAGE_CONFIRMATION, title, message, captions);
	}
	
	public static ButtonType showWarning(String message) {
		return show(AlertType.WARNING, null, IMAGE_WARNING, null, message, null);
	}
	
	public static ButtonType showWarning(String title, String message) {
		return show(AlertType.WARNING, null, IMAGE_WARNING, title, message, null);
	}
	
	public static ButtonType showWarning(Window owner, String message) {
		return show(AlertType.WARNING, owner, IMAGE_WARNING, null, message, null);
	}
	
	public static ButtonType showWarning(Window owner, String title, String message) {
		return show(AlertType.WARNING, owner, IMAGE_WARNING, title, message, null);
	}
	
	public static ButtonType showError(String message) {
		return show(AlertType.ERROR, null, IMAGE_ERROR, null, message, null);
	}
	
	public static ButtonType showError(String title, String message) {
		return show(AlertType.ERROR, null, IMAGE_ERROR, title, message, null);
	}
	
	public static ButtonType showError(Window owner, String message) {
		return show(AlertType.ERROR, owner, IMAGE_ERROR, null, message, null);
	}
	
	public static ButtonType showError(Window owner, String title, String message) {
		return show(AlertType.ERROR, owner, IMAGE_ERROR, title, message, null);
	}
	
	public static ButtonType show(AlertType type, final Window owner, Image icon, String title, String message, Map<ButtonType, String> captions) {
		final Alert dialog = new Alert(type);
		
		if(owner instanceof Stage) {
			ObservableList<Image> icons = ((Stage)owner).getIcons();
			if(icons != null && icons.size() > 0) {
				Stage stage = (Stage)dialog.getDialogPane().getScene().getWindow();
				stage.getIcons().add(icons.get(0));
			}
			if(title == null) {
				title = ((Stage)owner).getTitle();
			}
		}
		
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		if(icon != null) {
			dialog.setGraphic(new ImageView(icon));
		}
		dialog.setContentText(message);

		if(captions != null) {
			for(Entry<ButtonType, String> caption : captions.entrySet()) {
				Node button = dialog.getDialogPane().lookupButton(caption.getKey());
				if(button instanceof Button) {
					((Button)button).setText(caption.getValue());
				}
			}
		}
		
		if(owner != null) {
			dialog.getDialogPane().layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
				@Override
				public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
					if(dialog.getWidth() > 0 && dialog.getHeight() > 0) {
						double x = owner.getX() + owner.getWidth() / 2;
						double y = owner.getY() + owner.getHeight() / 2;
						dialog.setX(x - dialog.getWidth() / 2);
						dialog.setY(y - dialog.getHeight() / 2);
						dialog.getDialogPane().layoutBoundsProperty().removeListener(this);
					}
				}
			});
		}
		
		Optional<ButtonType> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : null;
	}
	
	
}
