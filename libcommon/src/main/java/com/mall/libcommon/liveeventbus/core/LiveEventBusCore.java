package com.mall.libcommon.liveeventbus.core;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ExternalLiveData;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.mall.libcommon.base.BaseApplicationKt;
import com.mall.libcommon.ext.LogExtKt;
import com.mall.libcommon.ext.ThreadExtKt;
import com.mall.libcommon.liveeventbus.ipc.consts.IpcConst;
import com.mall.libcommon.liveeventbus.ipc.core.ProcessorManager;
import com.mall.libcommon.liveeventbus.ipc.receiver.LebIpcReceiver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LiveEventBusCore {
    /**
     * 单例模式实现
     */
    private static class SingletonHolder {
        private static final LiveEventBusCore DEFAULT_BUS = new LiveEventBusCore();
    }

    public static LiveEventBusCore get() {
        return SingletonHolder.DEFAULT_BUS;
    }

    /**
     * 存放LiveEvent
     */
    private final Map<String, LiveEvent<Object>> bus;

    /**
     * 可配置的项
     */
    private final Config config = new Config();
    private boolean lifecycleObserverAlwaysActive;
    private boolean autoClear;
    private final Map<String, ObservableConfig> observableConfigs;

    /**
     * 跨进程通信
     */
    private LebIpcReceiver receiver;
    private boolean isRegisterReceiver = false;

    /**
     * 调试
     */
    final InnerConsole console = new InnerConsole();

    private LiveEventBusCore() {
        bus = new HashMap<>();
        observableConfigs = new HashMap<>();
        lifecycleObserverAlwaysActive = true;
        autoClear = false;
        receiver = new LebIpcReceiver();
        registerReceiver();
    }

    public synchronized <T> Observable<T> with(String key, Class<T> type) {
        if (!bus.containsKey(key)) {
            bus.put(key, new LiveEvent<>(key));
        }
        return (Observable<T>) bus.get(key);
    }

    public Config config() {
        return config;
    }

    public ObservableConfig config(String key) {
        if (!observableConfigs.containsKey(key)) {
            observableConfigs.put(key, new ObservableConfig());
        }
        return observableConfigs.get(key);
    }

    void registerReceiver() {
        if (isRegisterReceiver) {
            return;
        }
        Application application = BaseApplicationKt.getApplication();
        if (application != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(IpcConst.ACTION);
            application.registerReceiver(receiver, intentFilter);
            isRegisterReceiver = true;
        }
    }

    void setLifecycleObserverAlwaysActive(boolean lifecycleObserverAlwaysActive) {
        this.lifecycleObserverAlwaysActive = lifecycleObserverAlwaysActive;
    }

    void setAutoClear(boolean autoClear) {
        this.autoClear = autoClear;
    }

    private class LiveEvent<T> implements Observable<T> {

        @NonNull
        private final String key;
        private final LifecycleLiveData<T> liveData;
        private final Map<Observer, ObserverWrapper<T>> observerMap = new HashMap<>();
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        LiveEvent(@NonNull String key) {
            this.key = key;
            this.liveData = new LifecycleLiveData<>(key);
        }

        /**
         * 进程内发送消息
         *
         * @param value 发送的消息
         */
        @Override
        public void post(T value) {
            if (ThreadExtKt.isMainThread()) {
                postInternal(value);
            } else {
                mainHandler.post(new PostValueTask(value));
            }
        }

        /**
         * App内发送消息，跨进程使用
         *
         * @param value 发送的消息
         */
        @Override
        public void postAcrossProcess(T value) {
            broadcast(value, false, true);
        }

        /**
         * App之间发送消息
         *
         * @param value 发送的消息
         */
        @Override
        public void postAcrossApp(T value) {
            broadcast(value, false, false);
        }

        /**
         * 进程内发送消息，延迟发送
         *
         * @param value 发送的消息
         * @param delay 延迟毫秒数
         */
        @Override
        public void postDelay(T value, long delay) {
            mainHandler.postDelayed(new PostValueTask(value), delay);
        }

        /**
         * 进程内发送消息，延迟发送，带生命周期
         * 如果延时发送消息的时候sender处于非激活状态，消息取消发送
         *
         * @param owner 消息发送者
         * @param value 发送的消息
         * @param delay 延迟毫秒数
         */
        @Override
        public void postDelay(LifecycleOwner owner, final T value, long delay) {
            mainHandler.postDelayed(new PostLifeValueTask(value, owner), delay);
        }

        /**
         * 进程内发送消息
         * 强制接收到消息的顺序和发送顺序一致
         *
         * @param value 发送的消息
         */
        @Override
        public void postOrderly(T value) {
            mainHandler.post(new PostValueTask(value));
        }

        /**
         * App之间发送消息
         *
         * @param value 发送的消息
         */
        @Override
        @Deprecated
        public void broadcast(T value) {
            broadcast(value, false, false);
        }

        /**
         * 以广播的形式发送一个消息
         * 需要跨进程、跨APP发送消息的时候调用该方法
         *
         * @param value      发送的消息
         * @param foreground true:前台广播、false:后台广播
         * @param onlyInApp  true:只在APP内有效、false:全局有效
         */
        @Override
        public void broadcast(final T value, final boolean foreground, final boolean onlyInApp) {
            BaseApplicationKt.getApplication();
            if (ThreadExtKt.isMainThread()) {
                broadcastInternal(value, foreground, onlyInApp);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        broadcastInternal(value, foreground, onlyInApp);
                    }
                });
            }
        }

        /**
         * 注册一个Observer，生命周期感知，自动取消订阅
         *
         * @param owner    LifecycleOwner
         * @param observer 观察者
         */
        @Override
        public void observe(@NonNull final LifecycleOwner owner, @NonNull final Observer<T> observer) {
            if (ThreadExtKt.isMainThread()) {
                observeInternal(owner, observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observeInternal(owner, observer);
                    }
                });
            }
        }

        /**
         * 注册一个Observer，生命周期感知，自动取消订阅
         * 如果之前有消息发送，可以在注册时收到消息（消息同步）
         *
         * @param owner    LifecycleOwner
         * @param observer 观察者
         */
        @Override
        public void observeSticky(@NonNull final LifecycleOwner owner, @NonNull final Observer<T> observer) {
            if (ThreadExtKt.isMainThread()) {
                observeStickyInternal(owner, observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observeStickyInternal(owner, observer);
                    }
                });
            }
        }

        /**
         * 注册一个Observer，需手动解除绑定
         *
         * @param observer 观察者
         */
        @Override
        public void observeForever(@NonNull final Observer<T> observer) {
            if (ThreadExtKt.isMainThread()) {
                observeForeverInternal(observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observeForeverInternal(observer);
                    }
                });
            }
        }

        /**
         * 注册一个Observer，需手动解除绑定
         * 如果之前有消息发送，可以在注册时收到消息（消息同步）
         *
         * @param observer 观察者
         */
        @Override
        public void observeStickyForever(@NonNull final Observer<T> observer) {
            if (ThreadExtKt.isMainThread()) {
                observeStickyForeverInternal(observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observeStickyForeverInternal(observer);
                    }
                });
            }
        }

        /**
         * 通过observeForever或observeStickyForever注册的，需要调用该方法取消订阅
         *
         * @param observer 观察者
         */
        @Override
        public void removeObserver(@NonNull final Observer<T> observer) {
            if (ThreadExtKt.isMainThread()) {
                removeObserverInternal(observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeObserverInternal(observer);
                    }
                });
            }
        }

        @MainThread
        private void postInternal(T value) {
            LogExtKt.logi("post: " + value + " with key: " + key);
            liveData.setValue(value);
        }

        @MainThread
        private void broadcastInternal(T value, boolean foreground, boolean onlyInApp) {
            LogExtKt.logi("broadcast: " + value + " foreground: " + foreground +
                    " with key: " + key);
            Application application = BaseApplicationKt.getApplication();
            Intent intent = new Intent(IpcConst.ACTION);
            if (foreground) {
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            }
            if (onlyInApp) {
                intent.setPackage(application.getPackageName());
            }
            intent.putExtra(IpcConst.KEY, key);
            boolean handle = ProcessorManager.getManager().writeTo(intent, value);
            try {
                if (handle) {
                    application.sendBroadcast(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @MainThread
        private void observeInternal(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            observerWrapper.preventNextEvent = liveData.getVersion() > ExternalLiveData.START_VERSION;
            liveData.observe(owner, observerWrapper);
            LogExtKt.logi("observe observer: " + observerWrapper + "(" + observer + ")"
                    + " on owner: " + owner + " with key: " + key);
        }

        @MainThread
        private void observeStickyInternal(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            liveData.observe(owner, observerWrapper);
            LogExtKt.logi("observe sticky observer: " + observerWrapper + "(" + observer + ")"
                    + " on owner: " + owner + " with key: " + key);
        }

        @MainThread
        private void observeForeverInternal(@NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            observerWrapper.preventNextEvent = liveData.getVersion() > ExternalLiveData.START_VERSION;
            observerMap.put(observer, observerWrapper);
            liveData.observeForever(observerWrapper);
            LogExtKt.logi("observe forever observer: " + observerWrapper + "(" + observer + ")"
                    + " with key: " + key);
        }

        @MainThread
        private void observeStickyForeverInternal(@NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            observerMap.put(observer, observerWrapper);
            liveData.observeForever(observerWrapper);
            LogExtKt.logi("observe sticky forever observer: " + observerWrapper + "(" + observer + ")"
                    + " with key: " + key);
        }

        @MainThread
        private void removeObserverInternal(@NonNull Observer<T> observer) {
            Observer<T> realObserver;
            if (observerMap.containsKey(observer)) {
                realObserver = observerMap.remove(observer);
            } else {
                realObserver = observer;
            }
            liveData.removeObserver(realObserver);
        }

        private class LifecycleLiveData<T> extends ExternalLiveData<T> {

            private final String key;

            public LifecycleLiveData(String key) {
                this.key = key;
            }

            @Override
            protected Lifecycle.State observerActiveLevel() {
                return lifecycleObserverAlwaysActive() ? Lifecycle.State.CREATED : Lifecycle.State.STARTED;
            }

            @Override
            public void removeObserver(@NonNull Observer<? super T> observer) {
                super.removeObserver(observer);
                if (autoClear() && !liveData.hasObservers()) {
                    LiveEventBusCore.get().bus.remove(key);
                }
                LogExtKt.logi("observer removed: " + observer);
            }

            private boolean lifecycleObserverAlwaysActive() {
                if (observableConfigs.containsKey(key)) {
                    ObservableConfig config = observableConfigs.get(key);
                    if (config.lifecycleObserverAlwaysActive != null) {
                        return config.lifecycleObserverAlwaysActive;
                    }
                }
                return lifecycleObserverAlwaysActive;
            }

            private boolean autoClear() {
                if (observableConfigs.containsKey(key)) {
                    ObservableConfig config = observableConfigs.get(key);
                    if (config.autoClear != null) {
                        return config.autoClear;
                    }
                }
                return autoClear;
            }
        }

        private class PostValueTask implements Runnable {
            private Object newValue;

            public PostValueTask(@NonNull Object newValue) {
                this.newValue = newValue;
            }

            @Override
            public void run() {
                postInternal((T) newValue);
            }
        }

        private class PostLifeValueTask implements Runnable {
            private Object newValue;
            private LifecycleOwner owner;

            public PostLifeValueTask(@NonNull Object newValue, @Nullable LifecycleOwner owner) {
                this.newValue = newValue;
                this.owner = owner;
            }

            @Override
            public void run() {
                if (owner != null) {
                    if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        postInternal((T) newValue);
                    }
                }
            }
        }
    }

    private class ObserverWrapper<T> implements Observer<T> {

        @NonNull
        private final Observer<T> observer;
        private boolean preventNextEvent = false;

        ObserverWrapper(@NonNull Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged(@Nullable T t) {
            if (preventNextEvent) {
                preventNextEvent = false;
                return;
            }
            LogExtKt.logi("message received: " + t);
            try {
                observer.onChanged(t);
            } catch (ClassCastException e) {
                LogExtKt.logw("class cast error on message received: " + t);
            } catch (Exception e) {
                LogExtKt.logw("error on message received: " + t);
            }
        }
    }

    class InnerConsole {

        String getConsoleInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("*********Base info*********").append("\n");
            sb.append(getBaseInfo());
            sb.append("*********Event info*********").append("\n");
            sb.append(getBusInfo());
            return sb.toString();
        }

        String getBaseInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("lifecycleObserverAlwaysActive: ").append(lifecycleObserverAlwaysActive).append("\n")
                    .append("autoClear: ").append(autoClear).append("\n")
                    .append("logger enable: ").append(LogExtKt.getLoggerEnable()).append("\n")
                    .append("logger: ").append(LogExtKt.getLoggerEnable()).append("\n")
                    .append("Receiver register: ").append(isRegisterReceiver).append("\n")
                    .append("Application: ").append(BaseApplicationKt.getApplication()).append("\n");
            return sb.toString();
        }

        String getBusInfo() {
            StringBuilder sb = new StringBuilder();
            for (String key : bus.keySet()) {
                sb.append("Event name: " + key).append("\n");
                ExternalLiveData liveData = bus.get(key).liveData;
                sb.append("\tversion: " + liveData.getVersion()).append("\n");
                sb.append("\thasActiveObservers: " + liveData.hasActiveObservers()).append("\n");
                sb.append("\thasObservers: " + liveData.hasObservers()).append("\n");
                sb.append("\tActiveCount: " + getActiveCount(liveData)).append("\n");
                sb.append("\tObserverCount: " + getObserverCount(liveData)).append("\n");
                sb.append("\tObservers: ").append("\n");
                sb.append("\t\t" + getObserverInfo(liveData)).append("\n");
            }
            return sb.toString();
        }

        private int getActiveCount(LiveData liveData) {
            try {
                Field field = LiveData.class.getDeclaredField("mActiveCount");
                field.setAccessible(true);
                return (int) field.get(liveData);
            } catch (Exception e) {
                return -1;
            }
        }

        private int getObserverCount(LiveData liveData) {
            try {
                Field field = LiveData.class.getDeclaredField("mObservers");
                field.setAccessible(true);
                Object mObservers = field.get(liveData);
                Class<?> classOfSafeIterableMap = mObservers.getClass();
                Method size = classOfSafeIterableMap.getDeclaredMethod("size");
                size.setAccessible(true);
                return (int) size.invoke(mObservers);
            } catch (Exception e) {
                return -1;
            }
        }

        private String getObserverInfo(LiveData liveData) {
            try {
                Field field = LiveData.class.getDeclaredField("mObservers");
                field.setAccessible(true);
                Object mObservers = field.get(liveData);
                return mObservers.toString();
            } catch (Exception e) {
                return "";
            }
        }
    }
}
