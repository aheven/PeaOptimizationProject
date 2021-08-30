package com.mall.libcommon.ext

import android.widget.Toast

fun toast(message:String){
    Toast.makeText(getTopActivity(), message, Toast.LENGTH_SHORT).show()
}