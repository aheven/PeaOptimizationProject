package com.mall.libcommon.liveeventbus.ipc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mall.libcommon.liveeventbus.LiveEventBus;
import com.mall.libcommon.liveeventbus.ipc.consts.IpcConst;
import com.mall.libcommon.liveeventbus.ipc.core.ProcessorManager;

public class LebIpcReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (IpcConst.ACTION.equals(intent.getAction())) {
            try {
                String key = intent.getStringExtra(IpcConst.KEY);
                Object value = ProcessorManager.getManager().createFrom(intent);
                if (key != null && value != null) {
                    LiveEventBus.get(key).post(value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
