package com.mall.libcommon.liveeventbus.ipc.annotation;

import com.mall.libcommon.liveeventbus.ipc.core.Processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IpcConfig {
    Class<? extends Processor> processor();
}
