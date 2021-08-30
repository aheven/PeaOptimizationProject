package com.mall.libcommon.permission

object Permission {
    /**
     * 外部存储权限（特殊权限，需要Android 11 及以上）
     * 如果你的应用需要上架 GooglePlay，那么需要详细查看：https://support.google.com/googleplay/android-developer/answer/9956427
     */
    const val MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE"

    /**
     * 安装应用权限（特殊权限，需要Android 8.0 及以上）
     * Android 11 特性调整，安装外部来源应用需要重启App：https://cloud.tencent.com/developer/news/637591
     */
    const val REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES"

    /**
     * 通知栏权限（特殊权限，需要Android 6.0 及以上，注意此权限不需要在清单文件中注册也能申请）
     */
    const val NOTIFICATION_SERVICE = "android.permission.NOTIFICATION_SERVICE"

    /**
     * 悬浮窗权限（特殊权限，需要Android 6.0 及以上）
     *
     * 在Android 10 及之前的版本能跳转到应用悬浮窗设置页面，而在Android 11 及之后的版本只能跳转到系统设置悬浮窗管理列表了
     * 具体详情请看官方文档解释：https://developer.android.google.cn/reference/android/provider/Settings#ACTION_MANAGE_OVERLAY_PERMISSION
     */
    const val SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW"

    /**
     * 系统设置权限（特殊权限，需要Android 6.0 及以上）
     */
    const val WRITE_SETTINGS = "android.permission.WRITE_SETTINGS"

    /** 读取外部存储 */
    const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"

    /** 写入外部存储 */
    const val WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"

    /** 相机权限 */
    const val CAMERA = "android.permission.CAMERA"

    /** 麦克风权限 */
    const val RECORD_AUDIO = "android.permission.RECORD_AUDIO"

    /** 获取精确位置 */
    const val ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"

    /** 获取粗略位置 */
    const val ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"

    /**
     * 在后台获取位置（需要 Android 10.0 及以上）
     *
     * 需要注意的是：如果你的 App 只在前台状态下使用定位功能，请不要申请该权限
     */
    const val ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION"

    /** 读取联系人 */
    const val READ_CONTACTS = "android.permission.READ_CONTACTS"

    /** 修改联系人 */
    const val WRITE_CONTACTS = "android.permission.WRITE_CONTACTS"

    /** 访问账户列表 */
    const val GET_ACCOUNTS = "android.permission.GET_ACCOUNTS"

    /** 读取日历 */
    const val READ_CALENDAR = "android.permission.READ_CALENDAR"

    /** 修改日历 */
    const val WRITE_CALENDAR = "android.permission.WRITE_CALENDAR"

    /**
     * 读取照片中的地理位置（需要 Android 10.0 及以上）
     *
     * 需要注意的是：如果这个权限申请成功了但是不能正常读取照片的地理信息，那么需要先申请存储权限：
     *
     * 如果项目 targetSdkVersion <= 29 需要申请 {@link Permission.Group#STORAGE}
     * 如果项目 targetSdkVersion >= 30 需要申请 {@link Permission#MANAGE_EXTERNAL_STORAGE}
     */
    const val ACCESS_MEDIA_LOCATION = "android.permission.ACCESS_MEDIA_LOCATION"

    /**
     * 读取电话状态
     *
     * 需要注意的是：这个权限在某些手机上是没办法获取到的，因为某些系统禁止应用获得该权限
     *            所以很可能申请了这个权限之后没有弹授权框，而是直接回调失败方法
     * 后续情况汇报：有人反馈在 iQOO 手机上面获取不到该权限，在清单文件加入下面这个权限就可以了（这里只是做记录，并不代表这种方式就一定有效果）
     *              <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE" />
     */
    const val READ_PHONE_STATE = "android.permission.READ_PHONE_STATE"

    /** 拨打电话 */
    const val CALL_PHONE = "android.permission.CALL_PHONE"

    /** 读取通话记录 */
    const val READ_CALL_LOG = "android.permission.READ_CALL_LOG"

    /** 修改通话记录 */
    const val WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG"

    /** 添加语音邮件 */
    const val ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL"

    /** 使用SIP视频 */
    const val USE_SIP = "android.permission.USE_SIP"

    /**
     * 处理播出电话
     *
     * @deprecated  在 Android 10 已经过时，请见：https://developer.android.google.cn/reference/android/Manifest.permission?hl=zh_cn#PROCESS_OUTGOING_CALLS
     */
    const val PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS"

    /**
     * 接听电话（需要 Android 8.0 及以上，Android 8.0 以下可以采用模拟耳机按键事件来实现接听电话，这种方式不需要权限）
     */
    const val ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS"

    /** 读取手机号码（需要 Android 8.0 及以上） */
    const val READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS"

    /** 使用传感器 */
    const val BODY_SENSORS = "android.permission.BODY_SENSORS"

    /** 获取活动步数（需要 Android 10.0 及以上）  */
    const val ACTIVITY_RECOGNITION = "android.permission.ACTIVITY_RECOGNITION"

    /** 发送短信  */
    const val SEND_SMS = "android.permission.SEND_SMS"

    /** 接收短信  */
    const val RECEIVE_SMS = "android.permission.RECEIVE_SMS"

    /** 读取短信  */
    const val READ_SMS = "android.permission.READ_SMS"

    /** 接收 WAP 推送消息  */
    const val RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH"

    /** 接收彩信  */
    const val RECEIVE_MMS = "android.permission.RECEIVE_MMS"

    /** 允许呼叫应用继续在另一个应用中启动的呼叫（需要 Android 9.0 及以上）  */
    const val ACCEPT_HANDOVER = "android.permission.ACCEPT_HANDOVER"

    /** 存储权限  */
    val STORAGE = arrayOf(
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE
    )

    /** 日历权限  */
    val CALENDAR = arrayOf(
        READ_CALENDAR,
        WRITE_CALENDAR
    )

    /** 联系人权限  */
    val CONTACTS = arrayOf(
        READ_CONTACTS,
        WRITE_CONTACTS,
        GET_ACCOUNTS
    )

    /** 传感器权限  */
    val SENSORS = arrayOf(
        BODY_SENSORS,
        ACTIVITY_RECOGNITION
    )
}