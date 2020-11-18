package net.osdn.util.javafx.scene.control;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Dialogs {

	protected static final Map<ButtonType, String> buttonTexts = new HashMap<ButtonType, String>();

	static {
		String BASE_NAME = "com/sun/javafx/scene/control/skin/resources/controls";
		Map<ButtonType, String> keys = new LinkedHashMap<ButtonType, String>();
		keys.put(ButtonType.APPLY, "Dialog.apply.button");
		keys.put(ButtonType.OK, "Dialog.ok.button");
		keys.put(ButtonType.CANCEL, "Dialog.cancel.button");
		keys.put(ButtonType.CLOSE, "Dialog.close.button");
		keys.put(ButtonType.YES, "Dialog.yes.button");
		keys.put(ButtonType.NO, "Dialog.no.button");
		keys.put(ButtonType.FINISH, "Dialog.finish.button");
		keys.put(ButtonType.NEXT, "Dialog.next.button");
		keys.put(ButtonType.PREVIOUS, "Dialog.previous.button");

		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle(BASE_NAME);
		} catch(MissingResourceException ignore) {}

		if(bundle != null) {
			for(Map.Entry<ButtonType, String> entry : keys.entrySet()) {
				try {
					buttonTexts.put(entry.getKey(), bundle.getString(entry.getValue()));
				} catch(MissingResourceException ignore) {}
			}
		}
	}

	public static ButtonType showInformation(String message) {
		return show(AlertType.INFORMATION, null, null, null, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showInformation(String title, String message) {
		return show(AlertType.INFORMATION, null, null, title, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showInformation(Window owner, String message) {
		return show(AlertType.INFORMATION, owner, null, null, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showInformation(Window owner, String title, String message) {
		return show(AlertType.INFORMATION, owner, null, title, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showConfirmation(String message) {
		return show(AlertType.CONFIRMATION, null, null, null, message, new ButtonType[] { ButtonType.YES, ButtonType.NO });
	}

	public static ButtonType showConfirmation(String title, String message) {
		return show(AlertType.CONFIRMATION, null, null, title, message, new ButtonType[] { ButtonType.YES, ButtonType.NO });
	}

	public static ButtonType showConfirmation(Window owner, String message) {
		return show(AlertType.CONFIRMATION, owner, null, null, message, new ButtonType[] { ButtonType.YES, ButtonType.NO });
	}

	public static ButtonType showConfirmation(Window owner, String title, String message) {
		return show(AlertType.CONFIRMATION, owner, null, title, message, new ButtonType[] { ButtonType.YES, ButtonType.NO });
	}

	public static ButtonType showWarning(String message) {
		return show(AlertType.WARNING, null, null, null, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showWarning(String title, String message) {
		return show(AlertType.WARNING, null, null, title, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showWarning(Window owner, String message) {
		return show(AlertType.WARNING, owner, null, null, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showWarning(Window owner, String title, String message) {
		return show(AlertType.WARNING, owner, null, title, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showError(String message) {
		return show(AlertType.ERROR, null, null, null, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showError(String title, String message) {
		return show(AlertType.ERROR, null, null, title, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showError(Window owner, String message) {
		return show(AlertType.ERROR, owner, null, null, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType showError(Window owner, String title, String message) {
		return show(AlertType.ERROR, owner, null, title, message, new ButtonType[] { ButtonType.OK });
	}

	public static ButtonType show(AlertType type, final Window owner, Node icon, String title, String message, ButtonType... buttons) {
		final Alert dialog = new Alert(type, message, buttons);

		if(owner != null && owner.getScene() != null) {
			dialog.initOwner(owner);
		}

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
			dialog.setGraphic(icon);
		}

		if(owner != null) {
			dialog.getDialogPane().layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
				@Override
				public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
					if(newValue != null && newValue.getWidth() > 0 && newValue.getHeight() > 0) {
						double x = owner.getX() + owner.getWidth() / 2;
						double y = owner.getY() + owner.getHeight() / 2;
						dialog.setX(x - newValue.getWidth() / 2);
						dialog.setY(y - newValue.getHeight() / 2);
						dialog.getDialogPane().layoutBoundsProperty().removeListener(this);
					}
				}
			});
		}

		dialog.showingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue) {
					DialogPane dialogPane = dialog.getDialogPane();
					for(ButtonType buttonType : dialogPane.getButtonTypes()) {
						String text = buttonTexts.get(buttonType);
						if(text != null) {
							Button button = (Button)dialogPane.lookupButton(buttonType);
							button.setText(text);
						}
					}
					dialog.showingProperty().removeListener(this);
				}
			}
		});

		Optional<ButtonType> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : null;
	}
}
