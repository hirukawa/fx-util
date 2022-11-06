package net.osdn.util.javafx.scene.image;

import javafx.scene.image.Image;

import java.io.InputStream;

public class ImageUtil {
	/*
	 * javafx.scene.image.Image インスタンスの生成時に java.lang.ExceptionInInitializerError がスローされたとの報告がありました。
	 * Thread.currentThread().getContextClassLoader() が null を返すことがあるのが原因のようです。
	 *
	 * Caused by: java.lang.NullPointerException: Cannot invoke "java.lang.ClassLoader.getResource(String)" because "<local1>" is null
	 * at javafx.graphics/javafx.scene.image.Image.validateUrl(Unknown Source)
	 * at javafx.graphics/javafx.scene.image.Image.<init>(Unknown Source)
	 * at net.osdn.aoiro.javafx.dialog.UpgradeDialog.<clinit>(UpgradeDialog.java:25)
	 *
	 * これを回避するために ImageUtil を用意しました。
	 * ImageUtil.getImage( ) では必要に応じて Thread.currentThread().setContextClassLoader() を呼び出して、
	 * Thread.currentThread().getContextClassLoader() が null を返さないようにします。
	 *
	 */

	public static Image getImage(String url) {
		boolean isContextClassLoaderReplaced = false;
		try {
			isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();
			return new Image(url);
		} finally {
			if(isContextClassLoaderReplaced) {
				Thread.currentThread().setContextClassLoader(null);
			}
		}
	}

	public static Image getImage(String url, boolean backgroundLoading) {
		boolean isContextClassLoaderReplaced = false;
		try {
			isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();
			return new Image(url, backgroundLoading);
		} finally {
			if(isContextClassLoaderReplaced) {
				Thread.currentThread().setContextClassLoader(null);
			}
		}
	}

	public static Image getImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
		boolean isContextClassLoaderReplaced = false;
		try {
			isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();
			return new Image(url, requestedWidth, requestedHeight, preserveRatio, smooth);
		} finally {
			if(isContextClassLoaderReplaced) {
				Thread.currentThread().setContextClassLoader(null);
			}
		}
	}

	public static Image getImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth, boolean backgroundLoading) {
		boolean isContextClassLoaderReplaced = false;
		try {
			isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();
			return new Image(url, requestedWidth, requestedHeight, preserveRatio, smooth, backgroundLoading);
		} finally {
			if(isContextClassLoaderReplaced) {
				Thread.currentThread().setContextClassLoader(null);
			}
		}
	}

	public static Image getImage(InputStream is) {
		boolean isContextClassLoaderReplaced = false;
		try {
			isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();
			return new Image(is);
		} finally {
			if(isContextClassLoaderReplaced) {
				Thread.currentThread().setContextClassLoader(null);
			}
		}
	}

	public static Image getImage(InputStream is, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth, boolean backgroundLoading) {
		boolean isContextClassLoaderReplaced = false;
		try {
			isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();
			return new Image(is, requestedWidth, requestedHeight, preserveRatio, smooth);
		} finally {
			if(isContextClassLoaderReplaced) {
				Thread.currentThread().setContextClassLoader(null);
			}
		}
	}

	private static boolean setContextClassLoaderIfNeeded() {
		//
		// macOS で FXML のロードに失敗するバグ対策です。
		//
		// Windows でもユーザーから報告されたスタックトレースで同様の事象が発生することがあるようです。
		// Caused by: java.lang.NullPointerException
		//     at javafx.fxml/javafx.fxml.JavaFXBuilderFactory.<init>(Unknown Source)
		//     at javafx.fxml/javafx.fxml.JavaFXBuilderFactory.<init>(Unknown Source)
		//     at javafx.fxml/javafx.fxml.FXMLLoader.<clinit>(Unknown Source)
		//
		// JavaFXBuilderFactoryのコンストラクタで NullPointerException が発生するのは FXMLLoader.getDefaultClassLoader() が null を返している可能性が考えられます。
		// FXMLLoader.getDefaultClassLoader() は FXMLLoader.setDefaultClassLoader( ) で明示的にクラスローダーが設定されていない場合、
		// Thread.currentThread().getContextClassLoader() を返します。これが null になっている可能性が考えられます。
		// その対策として Thread.currentThread().getContextClassLoader() が null の場合、システムクラスローダーを設定します。
		// ※ これが原因であるとの確証、および改善の保証はまだありません。
		//
		if(Thread.currentThread().getContextClassLoader() == null) {
			ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
			if(systemClassLoader != null) {
				Thread.currentThread().setContextClassLoader(systemClassLoader);
				return true;
			}
		}
		return false;
	}
}
