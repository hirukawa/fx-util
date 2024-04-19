package onl.oss.javafx.scene.control;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.InputMethodRequests;
import javafx.stage.Screen;

import java.awt.Toolkit;

/** WindowsでHiDPI（拡大率が100%よりも大きい）のときに、IME候補ウィンドウの表示位置がずれる問題を修正します。
 * IME候補ウィンドウは、拡大率を考慮していない位置に表示されてしまうので、表示位置に拡大率を掛けて補正しています。
 *
 */
public class HiDpiFixedInputMethodRequests implements InputMethodRequests {

	private static Boolean isWindows = null;
	private static Double scale = null;

	public static void fix(TextInputControl control) {
		if(control == null) {
			return;
		}
		if(!isWindows()) {
			return;
		}
		control.inputMethodRequestsProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue != null) {
				InputMethodRequests inputMethodRequest = control.getInputMethodRequests();
				if(inputMethodRequest != null && !(inputMethodRequest instanceof HiDpiFixedInputMethodRequests)) {
					control.setInputMethodRequests(new HiDpiFixedInputMethodRequests(inputMethodRequest));
				}
			}
		});
	}

	private static boolean isWindows() {
		// UIスレッドから呼ばれる前提なので同時実行制御不要です。
		if(isWindows == null) {
			String osName = System.getProperty("os.name", "").toLowerCase();
			isWindows = osName.startsWith("windows");
		}
		return isWindows;
	}

	private static double getScale() {
		// UIスレッドから呼ばれる前提なので同時実行制御不要です。
		if(scale == null) {
			if(isWindows()) {
				String uiScale = System.getProperty("glass.win.uiScale");
				if(uiScale != null) {
					try {
						if(uiScale.endsWith("%")) {
							uiScale = uiScale.substring(0, uiScale.length() - 1);
							scale = Integer.parseInt(uiScale) / 100d;
						} else {
							scale = Double.parseDouble(uiScale);
						}
					} catch(Exception ignore) {
					}
				}
				// glass.win.uiScale からスケールを計算する処理で例外が発生した場合、scale == null のままになっています。
				if(scale == null) {
					// Toolkit.getScreenResolution() は、拡大率100%のときに96を基準とした解像度を返します。
					// 拡大率125%のときは 120 が返されます。（120÷96=125%）
					// getScreenResolution() の値を 96 で割ることで拡大率を求めることができます。
					scale = Toolkit.getDefaultToolkit().getScreenResolution() / 96d;
				}
			}
			if(scale == null) {
				scale = 1.0;
			}
		}
		return scale;
	}


	private InputMethodRequests inputMethodRequests;

	public HiDpiFixedInputMethodRequests(InputMethodRequests inputMethodRequests) {
		this.inputMethodRequests = inputMethodRequests;
	}

	@Override
	public Point2D getTextLocation(int offset) {
		// テキスト入力欄が画面下部にあるときIMEの変換候補ウィンドウが上側に表示されます。
		// 上側に変換候補ウィンドウが表示されるときは Y座標を -15 しています。
		Point2D textLocation = inputMethodRequests.getTextLocation(offset);
		double x = textLocation.getX();
		double y = textLocation.getY();
		Screen screen = null;
		ObservableList<Screen> screens = Screen.getScreensForRectangle(x, y, 1, 1);
		if(screens.size() >= 1) {
			screen = screens.get(0);
		} else {
			screen = Screen.getPrimary();
		}

		y = Math.ceil(y);
		double diff = screen.getBounds().getMaxY() - y;

		//
		// Windows 10 と Windows 11 では IME変換ウィンドウの高さが異なります。
		// Windows 10 は IME変換ウィンドウの高さ 162（互換性オンで以前のバージョンのIMEを使うと高さ192。これには対応しません）
		// Windows 11 は IME 変換ウィンドウの高さ 192
		//
		// Windows 10 と Windows 11 では IME変換ウィンドウがタスクバーに重なる高さが異なります。
		// Windows 10 はタスクバーの高さ全体に IMEウィンドウが重なります。
		// Windows 11 はタスクバーの高さ上部 12px までしか IMEウィンドウが重なりません。（下部36pxには IMEウィンドウが重ならない）
		//
		String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("windows 10")) {
			if (diff < 164) {
				if (diff < 147) {
					y -= 15;
				} else {
					y -= 195;
				}
			}
		} else if (os.contains("windows 11")) {
			if (diff < 234) {
				if (diff < 234 - 15) {
					y -= 15;
				} else {
					y -= 228;
				}
			}
		} else {
			if (diff < 234) {
				if (diff < 234 - 15) {
					y -= 15;
				} else {
					y -= 228;
				}
			}
		}

		textLocation = new Point2D(x, y);
		return textLocation.multiply(getScale());
	}

	@Override
	public int getLocationOffset(int x, int y) {
		return (int)(inputMethodRequests.getLocationOffset(x, y) * getScale());
	}

	@Override
	public void cancelLatestCommittedText() {
		inputMethodRequests.cancelLatestCommittedText();
	}

	@Override
	public String getSelectedText() {
		return inputMethodRequests.getSelectedText();
	}
}
