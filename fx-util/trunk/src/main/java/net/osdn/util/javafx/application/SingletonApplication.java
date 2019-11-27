package net.osdn.util.javafx.application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import net.osdn.util.javafx.event.SilentCallback;
import net.osdn.util.javafx.event.SilentEventHandler;

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

    private static Class<? extends Application> appClass;
    private static AtomicInteger count = new AtomicInteger(0);
    private static CountDownLatch latch = new CountDownLatch(1);
    private static Stage primaryStage;

    public static void launch(Class<? extends Application> appClass, String... args) {
        SingletonApplication.appClass = appClass;
        try {
            if(primaryStage == null) {
                if(count.getAndIncrement() == 0) {
                    Application.launch(Interceptor.class, args);
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

    @SuppressWarnings("overloads")
    protected <T extends Event> EventHandler<T> wrap(SilentEventHandler<T> handler) {
        return SilentEventHandler.wrap(handler);
    }

    @SuppressWarnings("overloads")
    protected <P, R> Callback<P, R> wrap(SilentCallback<P, R> callback) {
        return SilentCallback.wrap(callback);
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
            stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    closeSplashScreen();
                    primaryStage.removeEventHandler(windowEvent.getEventType(), this);
                }
            });
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
