package net.osdn.util.javafx.scene.control;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import net.osdn.util.javafx.event.SilentCallable;
import net.osdn.util.javafx.event.SilentCallback;
import net.osdn.util.javafx.event.SilentChangeListener;
import net.osdn.util.javafx.event.SilentChangeListenerNewValueOnly;
import net.osdn.util.javafx.event.SilentChangeListenerWithoutObservable;
import net.osdn.util.javafx.event.SilentEventHandler;
import net.osdn.util.javafx.event.SilentInvalidationListener;
import net.osdn.util.javafx.event.SilentRunnable;

import java.util.concurrent.Callable;

public class DialogEx<R> extends Dialog<R> {

	public DialogEx(Window owner) {
		if(owner instanceof Stage) {
			Stage stage = (Stage)getDialogPane().getScene().getWindow();
			ObservableList<Image> icons = ((Stage)owner).getIcons();
			if (icons != null && icons.size() > 0) {
				stage.getIcons().add(icons.get(0));
			}
			stage.setTitle(((Stage)owner).getTitle());
		}
		if(owner != null) {
			getDialogPane().layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
				@Override
				public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
					if(newValue != null && newValue.getWidth() > 0 && newValue.getHeight() > 0) {
						double x = owner.getX() + owner.getWidth() / 2;
						double y = owner.getY() + owner.getHeight() / 2;
						setX(x - newValue.getWidth() / 2);
						setY(y - newValue.getHeight() / 2);
						getDialogPane().layoutBoundsProperty().removeListener(this);
					}
				}
			});
		}

		showingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue) {
					DialogPane dialogPane = getDialogPane();

					//ボタンのテキストを controls.properties の設定で上書きします。
					//この処理は初回のみ実行されます。リスナーが解除されるので 2回目以降の表示時には何もしません。
					for(ButtonType buttonType : dialogPane.getButtonTypes()) {
						String text = Dialogs.buttonTexts.get(buttonType);
						if(text != null) {
							Button button = (Button)dialogPane.lookupButton(buttonType);
							button.setText(text);
						}
					}
				}
				showingProperty().removeListener(this);
			}
		});

		showingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					//ダイアログがワークエリアに収まるように位置を調整します。
					//この処理はダイアログを表示するたびに実行されます。このリスナーは解除されません。
					Rectangle2D workarea = Screen.getPrimary().getVisualBounds();

					double x = getX();
					double y = getY();
					if(owner != null) {
						x = owner.getX() + owner.getWidth() / 2 - getWidth() / 2;
						y = owner.getY() + owner.getHeight() / 2 - getHeight() / 2;
					}
					double w = getWidth();
					double h = getHeight();
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
					if(x != getX()) {
						setX(x);
					}
					if (y != getY()) {
						setY(y);
					}
					if(w != getWidth()) {
						setWidth(w);
					}
					if(h != getHeight()) {
						setHeight(h);
					}
				}
			}
		});
	}

	/**
	 * このダイアログの幅と高さを、このダイアログのコンテンツ・サイズに一致するように設定します。
	 */
	public void sizeToScene() {
		DialogPane dialogPane = getDialogPane();
		if(dialogPane == null) {
			return;
		}
		Scene scene = dialogPane.getScene();
		if(scene == null) {
			return;
		}
		Window window = scene.getWindow();
		if(window == null) {
			return;
		}
		window.sizeToScene();
	}


	@SuppressWarnings("overloads")
	protected <T extends Event> EventHandler<T> wrap(SilentEventHandler<T> handler) {
		return SilentEventHandler.wrap(handler);
	}

	@SuppressWarnings("overloads")
	protected <P, R> Callback<P, R> wrap(SilentCallback<P, R> callback) {
		return SilentCallback.wrap(callback);
	}

	@SuppressWarnings("overloads")
	protected <T> ChangeListener<T> wrap(SilentChangeListener<T> listener) {
		return SilentChangeListener.wrap(listener);
	}

	@SuppressWarnings("overloads")
	protected <T> ChangeListener<T> wrap(SilentChangeListenerWithoutObservable<T> listener) {
		return SilentChangeListenerWithoutObservable.wrap(listener);
	}

	@SuppressWarnings("overloads")
	protected <T> ChangeListener<T> wrap(SilentChangeListenerNewValueOnly<T> listener) {
		return SilentChangeListenerNewValueOnly.wrap(listener);
	}

	@SuppressWarnings("overloads")
	protected InvalidationListener wrap(SilentInvalidationListener listener) {
		return SilentInvalidationListener.wrap(listener);
	}

	@SuppressWarnings("overloads")
	protected Runnable wrap(SilentRunnable runnable) {
		return SilentRunnable.wrap(runnable);
	}

	@SuppressWarnings("overloads")
	protected <V> Callable<V> wrap(SilentCallable<V> callable) {
		return SilentCallable.wrap(callable);
	}
}
