package com.sszabo.life_tok;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LifeTokApplication extends Application {
    public ExecutorService executorService = Executors.newFixedThreadPool(4);
    public Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
}
