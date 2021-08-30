package com.mall.libcommon.liveeventbus.ipc.core;

import android.os.Bundle;

import com.mall.libcommon.liveeventbus.ipc.consts.IpcConst;

import java.io.Serializable;

public class SerializableProcessor implements Processor {

    @Override
    public boolean writeToBundle(Bundle bundle, Object value) {
        if (!(value instanceof Serializable)) {
            return false;
        }
        bundle.putSerializable(IpcConst.KEY_VALUE, (Serializable) value);
        return true;
    }

    @Override
    public Object createFromBundle(Bundle bundle) {
        return bundle.getSerializable(IpcConst.KEY_VALUE);
    }
}
