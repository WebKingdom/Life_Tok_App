package com.sszabo.life_tok;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application class for Life Tok. Contains an executor service and a handler.
 */
public class LifeTokApplication extends Application {

    /**
     * Executor Service for running tasks asynchronously or off the main thread
     */
    public ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * The Handler for the main thread
     */
    public Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    /**
     * Initialize the executor and handler in case they have been shut down of modified.
     */
    public void initApp() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }

        if (mainThreadHandler == null) {
            mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
        }
    }
}
