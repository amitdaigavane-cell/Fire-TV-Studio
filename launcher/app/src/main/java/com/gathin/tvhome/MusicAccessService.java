package com.gathin.tvhome;

import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import java.util.concurrent.CopyOnWriteArraySet;

/** Optional media-session access only. Notification contents are never read or retained. */
public final class MusicAccessService extends NotificationListenerService {
    static final CopyOnWriteArraySet<Runnable> observers = new CopyOnWriteArraySet<>();
    private void changed() {
        new Handler(Looper.getMainLooper()).post(() -> {
            for (Runnable observer : observers) observer.run();
        });
    }
    @Override public void onListenerConnected() { changed(); }
    @Override public void onListenerDisconnected() { changed(); }
}
