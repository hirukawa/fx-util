package net.osdn.util.javafx.scene.control;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class ContextMenuUtil {

	private static Map<ContextMenu, EventHandler<WindowEvent>> fixEventHandlers = new WeakHashMap<ContextMenu, EventHandler<WindowEvent>>();

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
		for(Menu menu : menuBar.getMenus()) {
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
		for(MenuItem menuItem : menu.getItems()) {
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
		menuItem.parentPopupProperty().addListener((observable, oldValue, newValue) -> {
			if(oldValue != null) {
				EventHandler<WindowEvent> oldEventHandler = fixEventHandlers.get(oldValue);
				if(oldEventHandler != null) {
					oldValue.removeEventHandler(WindowEvent.WINDOW_SHOWING, oldEventHandler);
				}
			}
			if(newValue != null) {
				fix(newValue, false);
			}
		});
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
		EventHandler<WindowEvent> oldEventHandler = fixEventHandlers.get(contextMenu);
		if(oldEventHandler != null) {
			contextMenu.removeEventHandler(WindowEvent.WINDOW_SHOWING, oldEventHandler);
		}
		EventHandler<WindowEvent> newEventHandler = new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Node contextMenuNode = contextMenu.getStyleableNode();
				for (MenuItem menuItem : contextMenu.getItems()) {
					Node menuItemNode = menuItem.getStyleableNode();
					if(menuItemNode == null) {
						continue;
					}
					menuItemNode.setOnMouseExited(me -> {
						contextMenuNode.requestFocus();
					});
					menuItemNode.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
						if(!menuItemNode.isFocused()) {
							e.consume();
							if(hideOnMouseReleased) {
								contextMenu.hide();
							}
						}
					});
				}
				contextMenu.removeEventHandler(WindowEvent.WINDOW_SHOWING, this);
			}
		};
		contextMenu.addEventHandler(WindowEvent.WINDOW_SHOWING, newEventHandler);
		fixEventHandlers.put(contextMenu, newEventHandler);
		return contextMenu;
	}
}
