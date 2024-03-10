package onl.oss.javafx.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 二重起動を防止するシングルトン・アプリケーションの拡張元クラスです。
 * exewrapの拡張フラグSHAREを指定することを前提としています。
 *
 */
public abstract class SingletonApplication extends Application {

    private static Class<? extends Application> appClass;
    private static AtomicInteger count = new AtomicInteger(0);
    private static CountDownLatch latch = new CountDownLatch(1);
    private static volatile Stage primaryStage;
    private static volatile boolean isStopped = true;

    public static void launch(Class<? extends Application> appClass, String... args) {
        SingletonApplication.appClass = appClass;
        try {
            if(primaryStage == null) {
                if(count.getAndIncrement() == 0) {
                    isStopped = false;
                    try {
                        Application.launch(Interceptor.class, args);
                    } finally {
                        isStopped = true;
                    }
                    return;
                }
                latch.await();
            }
            Platform.runLater(() -> {
                if(primaryStage.isIconified()) {
                    primaryStage.setIconified(false);
                }
                primaryStage.toFront();
            });
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void launch(String... args) {
        if(SingletonApplication.appClass != null) {
            launch(appClass, args);
            return;
        }
        try {
            String callingClassName = findCallingClassName();
            Class<?> theClass = Class.forName(callingClassName, false,
                    Thread.currentThread().getContextClassLoader());
            if (Application.class.isAssignableFrom(theClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends Application> appClass = (Class<? extends Application>)theClass;
                launch(appClass, args);
            } else {
                throw new RuntimeException("Error: " + theClass
                        + " is not a subclass of javafx.application.Application");
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isStopped() {
        return isStopped;
    }

    protected static String findCallingClassName() throws ClassNotFoundException {
        // Figure out the right class to call
        StackTraceElement[] cause = Thread.currentThread().getStackTrace();

        boolean foundThisMethod = false;
        String callingClassName = null;
        for (StackTraceElement se : cause) {
            // Skip entries until we get to the entry for this class
            String className = se.getClassName();
            String methodName = se.getMethodName();
            if (foundThisMethod) {
                callingClassName = className;
                break;
            } else if("launch".equals(methodName)
                    && Application.class.isAssignableFrom(Class.forName(className))) {
                foundThisMethod = true;
            }
        }

        if (callingClassName == null) {
            throw new RuntimeException("Error: unable to determine Application class");
        }

        return callingClassName;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static class Interceptor extends Application {
        private Application app;

        public Interceptor() throws ReflectiveOperationException {
            Constructor<? extends Application> c = appClass.getConstructor();
            app = c.newInstance();
        }

        @Override
        public void init() throws Exception {
            app.init();
        }

        @Override
        public void start(Stage stage) throws Exception {
            primaryStage = stage;
            app.start(stage);
            latch.countDown();
        }

        @Override
        public void stop() throws Exception {
            app.stop();
        }
    }
}
