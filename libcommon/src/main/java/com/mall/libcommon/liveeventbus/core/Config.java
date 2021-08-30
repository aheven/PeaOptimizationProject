package com.mall.libcommon.liveeventbus.core;

import android.content.Context;

public class Config {
    /**
     * 生命周期观察者始终活跃
     * true: 观察者总是能收到消息
     * false: 观察者只有在resume时能收到消息
     */
    public Config lifecycleObserverAlwaysActive(boolean active) {
        LiveEventBusCore.get().setLifecycleObserverAlwaysActive(active);
        return this;
    }

    /**
     * true: 当没有观察者观测的时候清空livedata
     * false: 从不清除LiveData，除非app被杀死
     */
    public Config autoClear(boolean clear) {
        LiveEventBusCore.get().setAutoClear(clear);
        return this;
    }

    /**
     * 只有调用了此方法，才能发送广播信息
     */
    public Config setContext(Context context) {
        LiveEventBusCore.get().registerReceiver();
        return this;
    }
}
