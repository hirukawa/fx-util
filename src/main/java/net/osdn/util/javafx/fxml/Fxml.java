package net.osdn.util.javafx.fxml;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextInputControl;
import net.osdn.util.javafx.scene.control.ContextMenuUtil;
import net.osdn.util.javafx.scene.control.HiDpiFixedInputMethodRequests;

import java.io.IOException;
import java.io.UncheckedIOException;

public class Fxml {

    public static <T> T load(Class<?> cls) {
        boolean isContextClassLoaderReplaced = false;
        try {
            isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();

            String fxmlFilename = cls.getSimpleName() + ".fxml";
            FXMLLoader loader = new FXMLLoader(cls.getResource(fxmlFilename));
            try {
                T obj = loader.load();
                fix(obj);
                return obj;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        } finally {
            if(isContextClassLoaderReplaced) {
                Thread.currentThread().setContextClassLoader(null);
            }
        }
    }

    public static <T> T load(Object root, Class<?> cls) {
        boolean isContextClassLoaderReplaced = false;
        try {
            isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();

            String fxmlFilename = cls.getSimpleName() + ".fxml";
            FXMLLoader loader = new FXMLLoader(cls.getResource(fxmlFilename));
            loader.setRoot(root);
            try {
                T obj = loader.load();
                fix(obj);
                return obj;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        } finally {
            if(isContextClassLoaderReplaced) {
                Thread.currentThread().setContextClassLoader(null);
            }
        }
    }

    public static <T> T load(Object controller) {
        boolean isContextClassLoaderReplaced = false;
        try {
            isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();

            String fxmlFilename = controller.getClass().getSimpleName() + ".fxml";
            FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(fxmlFilename));
            loader.setController(controller);
            try {
                T obj = loader.load();
                fix(obj);
                return obj;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        } finally {
            if(isContextClassLoaderReplaced) {
                Thread.currentThread().setContextClassLoader(null);
            }
        }
    }

    public static <T> T load(Object root, Object controller) {
        Class<?> cls = null;
        if(controller != null) {
            cls = controller.getClass();
        } else if(root != null) {
            cls = root.getClass();
        } else {
            throw new NullPointerException();
        }

        boolean isContextClassLoaderReplaced = false;
        try {
            isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();

            String fxmlFilename = cls.getSimpleName() + ".fxml";
            FXMLLoader loader = new FXMLLoader(cls.getResource(fxmlFilename));
            if(root != null) {
                loader.setRoot(root);
            }
            if(controller != null) {
                loader.setController(controller);
            }
            try {
                T obj = loader.load();
                fix(obj);
                return obj;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        } finally {
            if(isContextClassLoaderReplaced) {
                Thread.currentThread().setContextClassLoader(null);
            }
        }
    }

    public static <T> T load(Object controller, String fxmlFilename) {
        boolean isContextClassLoaderReplaced = false;
        try {
            isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();

            FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(fxmlFilename));
            loader.setController(controller);
            try {
                T obj = loader.load();
                fix(obj);
                return obj;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        } finally {
            if(isContextClassLoaderReplaced) {
                Thread.currentThread().setContextClassLoader(null);
            }
        }
    }

    public static <T> T load(Object root, Object controller, String fxmlFilename) {
        Class<?> cls = null;
        if(controller != null) {
            cls = controller.getClass();
        } else if(root != null) {
            cls = root.getClass();
        } else {
            throw new NullPointerException();
        }

        boolean isContextClassLoaderReplaced = false;
        try {
            isContextClassLoaderReplaced = setContextClassLoaderIfNeeded();

            FXMLLoader loader = new FXMLLoader(cls.getResource(fxmlFilename));
            if(root != null) {
                loader.setRoot(root);
            }
            if(controller != null) {
                loader.setController(controller);
            }
            try {
                T obj = loader.load();
                fix(obj);
                return obj;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

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

    private static void fix(Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof MenuBar) {
            ContextMenuUtil.fix((MenuBar) obj);
        } else if (obj instanceof ContextMenu) {
            ContextMenuUtil.fix((ContextMenu) obj);
        } else if (obj instanceof TextInputControl) {
            HiDpiFixedInputMethodRequests.fix((TextInputControl) obj);
        } else if (obj instanceof Parent) {
            for (Node child : ((Parent) obj).getChildrenUnmodifiable()) {
                fix(child);
            }
        }
    }
}
