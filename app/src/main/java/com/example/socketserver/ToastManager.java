package com.example.socketserver;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import static android.os.Looper.getMainLooper;

/**
 * Created by AidenChang 2020/06/23
 */
public class ToastManager {
    private static final String TAG = ToastManager.class.getSimpleName();
    private Context context;

    public ToastManager(Context context) {
        this.context = context;
    }

    public void showShortToast(String message) {
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Error show Toast. Message is Null.");
        }
    }

    public void showLongToast(String message) {
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "Error show Toast. Message is Null.");
        }
    }

    public void showShortToastInThread(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "CurrentThreadName: " + Thread.currentThread().getName());
                Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    public void showLongToastInThread(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "CurrentThreadName: " + Thread.currentThread().getName());
                Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
}
