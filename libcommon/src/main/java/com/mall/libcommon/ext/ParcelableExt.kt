package com.mall.libcommon.ext

import android.os.Parcel
import android.os.Parcelable

fun Parcelable.toBytes(): ByteArray {
    val parcel = Parcel.obtain()
    writeToParcel(parcel, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}

fun <T> ByteArray.toParcelable(creator: Parcelable.Creator<T>): T {
    val parcel = Parcel.obtain()
    parcel.unmarshall(this, 0, size)
    parcel.setDataPosition(0)
    val result: T = creator.createFromParcel(parcel)
    parcel.recycle()
    return result
}