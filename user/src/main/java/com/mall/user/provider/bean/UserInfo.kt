package com.mall.user.provider.bean

data class UserInfo(
    val avatar: String?,//头像
    val bindPdd: Int?,//是否授权绑定拼多多：0:未绑定；1:已绑定；
    val expiresIn: String?,//过期时间
    val ifShowApply: Int?,//是否显示申请店主:0不显示 1显示
    val invitationCode: String?,//邀请码
    val isNewUser: Boolean?,//0元购悬浮框展示标记,true展示,false不展示
    val isService: Int?,//是否添加导师微信：1是、 2否
    val nickName: String?,//用户昵称
    val parentAvatar: String?,//上级头像
    val parentNickName: String?,//上级昵称
    val relationId: String?,//渠道Id
    val roleType: Int?,//角色类型：1会员，2店主，3服务商
    val userCode: Int?,//新用户code,501:跳转设置密码,502:跳转绑定手机号
    val userId: String?,//用户Id
    val userNo: String?,//用户编号
    val weChatNo: String?//导师微信
)