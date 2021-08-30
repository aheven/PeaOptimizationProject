package com.mall.libcommon.liveeventbus;

import androidx.annotation.NonNull;

import com.mall.libcommon.liveeventbus.core.Config;
import com.mall.libcommon.liveeventbus.core.LiveEvent;
import com.mall.libcommon.liveeventbus.core.LiveEventBusCore;
import com.mall.libcommon.liveeventbus.core.Observable;
import com.mall.libcommon.liveeventbus.core.ObservableConfig;

public final class LiveEventBus {
    /**
     * 通过key and type获取observable
     */
    public static <T> Observable<T> get(@NonNull String key, @NonNull Class<T> type) {
        return LiveEventBusCore.get().with(key, type);
    }

    /**
     * 通过key获取observable
     */
    public static <T> Observable<T> get(@NonNull String key) {
        return (Observable<T>)get(key, Object.class);
    }

    /**
     * 通过type获取observable
     */
    public static <T extends LiveEvent> Observable<T> get(@NonNull Class<T> eventType) {
        return get(eventType.getName(), eventType);
    }

    /**
     * 使用内部类Config来设置参数
     * 首先调用config获取Config的实例
     * 然后调用Config的方法配置LiveEventBus
     * 在Application.onCreate()中调用方法
     */
    public static Config config() {
        return LiveEventBusCore.get().config();
    }

    /**
     * 同上，针对单个key设置参数
     */
    public static ObservableConfig config(@NonNull String key) {
        return LiveEventBusCore.get().config(key);
    }
}
