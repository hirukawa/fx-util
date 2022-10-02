package net.osdn.util.javafx.application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.awt.SplashScreen;
import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 二重起動を防止するシングルトン・アプリケーションの拡張元クラスです。
 * exewrapの拡張フラグSHAREを指定することを前提としています。
 *
 */
public abstract class SingletonApplication extends Application {

    private static boolean isWindows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
    private static boolean isImplicitExit = true;
    private static Class<? extends Application> appClass;
    private static AtomicInteger count = new AtomicInteger(0);
    private static CountDownLatch latch = new CountDownLatch(1);
    private static volatile Thread fxApplicationThread;
    private static volatile Stage primaryStage;
    private static boolean isStopped = true;

    /** FXアプリケーションスレッド終了時に暗黙的にプロセスを終了するかを示します。
     * このメソッドが true を返す場合、FXアプリケーション終了時に System.exit(0); が呼び出されます。
     * 既定値は true です。
     *
     * @return 暗黙的にプロセスを終了する場合は true、そうでなければ false。
     */
    public static boolean isImplicitExit() {
        return isImplicitExit;
    }

    /** FXアプリケーションスレッド終了時に暗黙的にプロセスを終了するかどうかを設定します。
     * true を設定すると、FXアプリケーション終了時に System.exit(0); が呼び出されます。
     *
     * @param implicitExit 暗黙的にプロセスを終了する場合は true、そうでなければ false。
     */
    public static void setImplicitExit(boolean implicitExit) {
        isImplicitExit = implicitExit;
    }

    public static void launch(Class<? extends Application> appClass, String... args) {
        SingletonApplication.appClass = appClass;
        try {
            if(primaryStage == null) {
                if(count.getAndIncrement() == 0) {
                    isStopped = false;
                    try {
                        Application.launch(Interceptor.class, args);
                        try {
                            if(fxApplicationThread != null && fxApplicationThread.isAlive()) {
                                fxApplicationThread.join(1000);
                            }
                        } catch(Throwable ignore) {}
                    } finally {
                        isStopped = true;
                    }
                    if(isImplicitExit) {
                        System.exit(0);
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
            // Windows以外のOSではすぐにスプラッシュスクリーンを非表示にします。
            if(!isWindows) {
                SplashScreen splash = SplashScreen.getSplashScreen();
                if(splash != null) {
                    splash.close();
                }
            }

            app.init();
        }

        @Override
        public void start(Stage stage) throws Exception {
            fxApplicationThread = Thread.currentThread();
            primaryStage = stage;

            if(isWindows) {
                stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        Platform.runLater(() -> {
                            closeSplashScreen();
                        });
                        primaryStage.removeEventHandler(windowEvent.getEventType(), this);
                    }
                });
            }
            app.start(stage);
            latch.countDown();
        }

        @Override
        public void stop() throws Exception {
            app.stop();
        }

        protected static void closeSplashScreen() {
            long SPLASH_TIME_MILLIS = 1500;

            SplashScreen splash = SplashScreen.getSplashScreen();
            if(splash != null) {
                long startup = 0;
                String s = System.getProperty("java.application.startup");
                if(s != null) {
                    try {
                        startup = Instant.parse(s).toEpochMilli();
                    } catch(DateTimeParseException e) {
                        e.printStackTrace();
                    }
                }
                long elapsed = System.currentTimeMillis() - startup;
                long delay = Math.max(SPLASH_TIME_MILLIS - elapsed, 1L);
                new Timeline(new KeyFrame(Duration.millis(delay), onFinished -> {
                    splash.close();
                })).play();
            }
        }
    }
}
