package net.osdn.util.javafx.scene.control;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.util.Callback;
import net.osdn.util.javafx.event.SilentCallback;
import net.osdn.util.javafx.event.SilentChangeListener;
import net.osdn.util.javafx.event.SilentChangeListenerNewValueOnly;
import net.osdn.util.javafx.event.SilentChangeListenerWithoutObservable;
import net.osdn.util.javafx.event.SilentEventHandler;
import net.osdn.util.javafx.event.SilentInvalidationListener;

public class DialogEx<R> extends Dialog<R> {

	public DialogEx(Window owner) {

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
}