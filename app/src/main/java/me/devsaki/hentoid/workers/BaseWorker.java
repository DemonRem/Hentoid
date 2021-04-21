package me.devsaki.hentoid.workers;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.devsaki.hentoid.events.ServiceDestroyedEvent;
import me.devsaki.hentoid.util.notification.Notification;
import me.devsaki.hentoid.util.notification.NotificationManager;
import timber.log.Timber;


/**
 * Base class for all workers
 */
public abstract class BaseWorker extends Worker {

    private static final Map<Integer, Boolean> running = Collections.synchronizedMap(new HashMap<>());
    protected NotificationManager notificationManager;

    private final @IdRes
    int serviceId;

    protected static boolean isRunning(int serviceId) {
        Boolean isRunning = running.get(serviceId);
        return (isRunning != null && isRunning);
    }

    private boolean isRunning() {
        return isRunning(serviceId);
    }

    private void registerStart() {
        running.put(serviceId, true);
    }

    private void registerShutdown() {
        running.put(serviceId, false);
    }

    public BaseWorker(
            @NonNull Context context,
            @NonNull WorkerParameters parameters,
            @IdRes int serviceId) {
        super(context, parameters);
        this.serviceId = serviceId;

        initNotifications(context);

        Timber.w("%s worker created", this.getClass().getSimpleName());
    }

    @Override
    public void onStopped() {
        clear();
        super.onStopped();
    }

    private void initNotifications(Context context) {
        notificationManager = new NotificationManager(context, serviceId);
        notificationManager.cancel();
    }

    private void ensureLongRunning() {
        setForegroundAsync(notificationManager.buildForegroundInfo(getStartNotification()));
    }

    private void clear() {
        onClear();

        // Tell everyone the worker is shutting down
        registerShutdown();

        EventBus.getDefault().post(new ServiceDestroyedEvent(serviceId));
        EventBus.getDefault().unregister(this);

        if (notificationManager != null) notificationManager.cancel();

        Timber.d("%s worker destroyed", this.getClass().getSimpleName());
    }

    @NonNull
    @Override
    public Result doWork() {
        if (isRunning()) return Result.failure();
        registerStart();

        ensureLongRunning();
        try {
            getToWork(getInputData());
        } finally {
            clear();
        }
        return Result.success();
    }

    abstract Notification getStartNotification();

    abstract void onClear();

    abstract void getToWork(@NonNull Data input);
}