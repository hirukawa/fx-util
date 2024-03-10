package onl.oss.javafx.scene.control;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class ContextMenuUtil {

	private static Map<ObservableList<Menu>, ListChangeListener<Menu>> menuListChangeListeners = new WeakHashMap<>();
	private static Map<ObservableList<MenuItem>, ListChangeListener<MenuItem>> menuItemListChangeListeners = new WeakHashMap<>();
	private static Map<ReadOnlyObjectProperty<ContextMenu>, ChangeListener<ContextMenu>> parentPopupChangeListeners = new WeakHashMap<>();
	private static Map<ContextMenu, EventHandler<WindowEvent>> contextMenuWindowShowingEventHandlers = new WeakHashMap<>();
	private static Map<Node, EventHandler<MouseEvent>> menuItemNodeMouseExitedEventHandlers = new WeakHashMap<>();
	private static Map<Node, EventHandler<MouseEvent>> menuItemNodeMouseReleasedEventFilters = new WeakHashMap<>();

	/**
	 * 指定したメニューバーに関連するコンテキスト・メニューの振る舞いを修正します。
	 *
	 * @param menuBar メニューバー
	 * @return メニューバー
	 */
	public static MenuBar fix(MenuBar menuBar) {
		if(menuBar == null) {
			return null;
		}
		ObservableList<Menu> menus = menuBar.getMenus();
		ListChangeListener<Menu> oldListChangeListener = menuListChangeListeners.get(menus);
		if(oldListChangeListener != null) {
			menus.removeListener(oldListChangeListener);
			menuListChangeListeners.remove(menus);
		}
		ListChangeListener<Menu> newListChangeListener = new ListChangeListener<Menu>() {
			@Override
			public void onChanged(Change<? extends Menu> c) {
				while(c.next()) {
					if(c.wasAdded()) {
						for(Menu menu : c.getAddedSubList()) {
							fix(menu);
						}
					}
				}
			}
		};
		menus.addListener(newListChangeListener);
		menuListChangeListeners.put(menus, newListChangeListener);

		for(Menu menu : menus) {
			fix(menu);
		}
		return menuBar;
	}

	/**
	 * 指定したメニューに関連するコンテキスト・メニューの振る舞いを修正します。
	 *
	 * @param menu メニュー
	 * @return メニュー
	 */
	public static Menu fix(Menu menu) {
		if(menu == null) {
			return null;
		}
		ObservableList<MenuItem> menuItems = menu.getItems();
		ListChangeListener<MenuItem> oldListChangeListener = menuItemListChangeListeners.get(menuItems);
		if(oldListChangeListener != null) {
			menuItems.removeListener(oldListChangeListener);
			menuItemListChangeListeners.remove(menuItems);
		}
		ListChangeListener<MenuItem> newListChangeListener = new ListChangeListener<MenuItem>() {
			@Override
			public void onChanged(Change<? extends MenuItem> c) {
				while(c.next()) {
					if(c.wasAdded()) {
						for(MenuItem menuItem : c.getAddedSubList()) {
							fix(menuItem);
						}
					}
				}
			}
		};
		menuItems.addListener(newListChangeListener);
		menuItemListChangeListeners.put(menuItems, newListChangeListener);

		for(MenuItem menuItem : menuItems) {
			fix(menuItem);
		}
		return menu;
	}

	/**
	 * 指定したメニューアイテムに関連するコンテキスト・メニューの振る舞いを修正します。
	 *
	 * @param menuItem メニューアイテム
	 * @return メニューアイテム
	 */
	public static MenuItem fix(MenuItem menuItem) {
		if(menuItem == null) {
			return null;
		}
		ReadOnlyObjectProperty<ContextMenu> parentPopup = menuItem.parentPopupProperty();
		ChangeListener<ContextMenu> oldChangeListener = parentPopupChangeListeners.get(parentPopup);
		if(oldChangeListener != null) {
			parentPopup.removeListener(oldChangeListener);
			parentPopupChangeListeners.remove(parentPopup);
		}
		ChangeListener<ContextMenu> newChangeListener = new ChangeListener<ContextMenu>() {
			@Override
			public void changed(ObservableValue<? extends ContextMenu> observable, ContextMenu oldValue, ContextMenu newValue) {
				if(oldValue != null) {
					removeListeners(oldValue);
				}
				if(newValue != null) {
					fix(newValue, false);
				}
			}
		};
		parentPopup.addListener(newChangeListener);
		parentPopupChangeListeners.put(parentPopup, newChangeListener);

		return menuItem;
	}

	/**
	 * コンテキスト・メニューの振る舞いを修正します。
	 * <p>
	 * JavaFXのコンテキスト・メニューはマウスカーソルを外側へ移動させてもメニュー・アイテムの選択状態が維持されます。
	 * また、メニュー・アイテム上でマウスボタンを押下したままマウスドラッグを続けると、
	 * メニュー・アイテム外部などどこでマウスボタンを離してもメニュー・アイテムの選択アクションが発生します。
	 * これらの振る舞いは、Windowsネイティブのコンテキスト・メニューの振る舞いと異なり違和感を感じさせます。</p>
	 * <p>
	 * このメソッドは指定したコンテキスト・メニューの振る舞いを修正します。</p>
	 *
	 * @param contextMenu コンテキスト・メニュー
	 * @return コンテキスト・メニュー
	 */
	public static ContextMenu fix(ContextMenu contextMenu) {
		return fix(contextMenu, true);
	}

	/**
	 * コンテキスト・メニューの振る舞いを修正します。
	 * <p>
	 * JavaFXのコンテキスト・メニューはマウスカーソルを外側へ移動させてもメニュー・アイテムの選択状態が維持されます。
	 * また、メニュー・アイテム上でマウスボタンを押下したままマウスドラッグを続けると、
	 * メニュー・アイテム外部などどこでマウスボタンを離してもメニュー・アイテムの選択アクションが発生します。
	 * これらの振る舞いは、Windowsネイティブのコンテキスト・メニューの振る舞いと異なり違和感を感じさせます。</p>
	 * <p>
	 * このメソッドは指定したコンテキスト・メニューの振る舞いを修正します。</p>
	 *
	 * @param contextMenu コンテキスト・メニュー
	 * @param hideOnMouseReleased マウスボタンを離した際にメニューアイテムが選択されていなければコンテキスト・メニューを非表示にするかどうかを指定します。
	 * @return コンテキスト・メニュー
	 */
	public static ContextMenu fix(ContextMenu contextMenu, boolean hideOnMouseReleased) {
		if(contextMenu == null) {
			return null;
		}
		EventHandler<WindowEvent> oldEventHandler = contextMenuWindowShowingEventHandlers.get(contextMenu);
		if(oldEventHandler != null) {
			contextMenu.removeEventHandler(WindowEvent.WINDOW_SHOWING, oldEventHandler);
			contextMenuWindowShowingEventHandlers.remove(contextMenu);
		}
		EventHandler<WindowEvent> newEventHandler = new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Node contextMenuNode = contextMenu.getStyleableNode();
				for (MenuItem menuItem : contextMenu.getItems()) {
					Node menuItemNode = menuItem.getStyleableNode();
					fixImpl(contextMenu, contextMenuNode, menuItem, menuItemNode, hideOnMouseReleased);
				}
				// コンテキスト・メニュー表示時になぜか先頭のアイテムが選択状態になることがあるようなので、
				// コンテキスト・メニュー自体にフォーカス要求を出して、先頭アイテムが初期選択状態にならないようにします。
				contextMenuNode.requestFocus();
				contextMenu.removeEventHandler(WindowEvent.WINDOW_SHOWING, this);
				contextMenuWindowShowingEventHandlers.remove(contextMenu);
			}
		};
		contextMenu.addEventHandler(WindowEvent.WINDOW_SHOWING, newEventHandler);
		contextMenuWindowShowingEventHandlers.put(contextMenu, newEventHandler);
		return contextMenu;
	}

	private static void fixImpl(ContextMenu contextMenu, Node contextMenuNode, MenuItem menuItem, Node menuItemNode, boolean hideOnMouseReleased) {
		if(contextMenu == null) {
			return;
		}
		if(contextMenuNode == null) {
			return;
		}
		if(menuItem == null) {
			return;
		}
		if(menuItemNode == null) {
			return;
		}

		removeListeners(menuItemNode);

		EventHandler<MouseEvent> newEventHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				contextMenuNode.requestFocus();
			}
		};
		menuItemNode.addEventHandler(MouseEvent.MOUSE_EXITED, newEventHandler);
		menuItemNodeMouseExitedEventHandlers.put(menuItemNode, newEventHandler);

		EventHandler<MouseEvent> newEventFilter = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(!menuItemNode.isFocused()) {
					event.consume();
					if(hideOnMouseReleased) {
						contextMenu.hide();
					}
				}
			}
		};
		menuItemNode.addEventFilter(MouseEvent.MOUSE_RELEASED, newEventFilter);
		menuItemNodeMouseReleasedEventFilters.put(menuItemNode, newEventFilter);
	}

	private static void removeListeners(ContextMenu contextMenu) {
		if(contextMenu == null) {
			return;
		}
		for(MenuItem menuItem : contextMenu.getItems()) {
			Node menuItemNode = menuItem.getStyleableNode();
			removeListeners(menuItemNode);
		}
		EventHandler<WindowEvent> oldEventHandler = contextMenuWindowShowingEventHandlers.get(contextMenu);
		if(oldEventHandler != null) {
			contextMenu.removeEventHandler(WindowEvent.WINDOW_SHOWING, oldEventHandler);
			contextMenuWindowShowingEventHandlers.remove(contextMenu);
		}
	}

	private static void removeListeners(Node menuItemNode) {
		if(menuItemNode == null) {
			return;
		}
		EventHandler<MouseEvent> oldEventHandler = menuItemNodeMouseExitedEventHandlers.get(menuItemNode);
		if(oldEventHandler != null) {
			menuItemNode.removeEventHandler(MouseEvent.MOUSE_EXITED, oldEventHandler);
			menuItemNodeMouseExitedEventHandlers.remove(menuItemNode);
		}
		EventHandler<MouseEvent> oldEventFilter = menuItemNodeMouseReleasedEventFilters.get(menuItemNode);
		if(oldEventFilter != null) {
			menuItemNode.removeEventFilter(MouseEvent.MOUSE_RELEASED, oldEventFilter);
			menuItemNodeMouseReleasedEventFilters.remove(menuItemNode);
		}
	}
}
