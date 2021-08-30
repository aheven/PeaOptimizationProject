package com.mall.libcommon.liveeventbus.ipc.core;

import android.os.Bundle;

public interface Processor {
    boolean writeToBundle(Bundle bundle, Object value) throws Exception;

    Object createFromBundle(Bundle bundle) throws Exception;
}
