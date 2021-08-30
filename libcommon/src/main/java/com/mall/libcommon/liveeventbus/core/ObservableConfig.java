package com.mall.libcommon.liveeventbus.core;

public class ObservableConfig {

    Boolean lifecycleObserverAlwaysActive = null;
    Boolean autoClear = null;

    /**
     * 生命周期观察者始终活跃
     * true: 观察者总是能收到消息
     * false: 观察者只有在resume时能收到消息
     */
    public ObservableConfig lifecycleObserverAlwaysActive(boolean active) {
        lifecycleObserverAlwaysActive = active;
        return this;
    }

    /**
     * true: 当没有观察者观测的时候清空livedata
     * false: 从不清除LiveData，除非app被杀死
     */
    public ObservableConfig autoClear(boolean clear) {
        autoClear = clear;
        return this;
    }
}
