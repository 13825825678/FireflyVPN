package xyz.a202132.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * 版本更新响应
 */
data class UpdateInfo(
    @SerializedName("version")
    val version: String,
    
    @SerializedName("versionCode")
    val versionCode: Int,
    
    @SerializedName("downloadUrl")
    val downloadUrl: String,
    
    @SerializedName("changelog") val changelog: String,
    @SerializedName("is_force") val isForce: Int = 0
)

/**
 * 通知公告响应
 */
data class NoticeInfo(
    @SerializedName("hasNotice")
    val hasNotice: Boolean,
    
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("content")
    val content: String = "",
    
    @SerializedName("noticeId")
    val noticeId: String = "",
    
    @SerializedName("showOnce")
    val showOnce: Boolean = true,
    
    @SerializedName("backupNodes")
    val backupNodes: BackupNodeInfo? = null
)

/**
 * 备用节点信息
 */
data class BackupNodeInfo(
    @SerializedName("msg")
    val msg: String? = null,
    
    @SerializedName("url")
    val url: String? = null
)

/**
 * 节点出口 IP 信息
 */
data class NodeIpInfo(
    @SerializedName("ip")
    val ip: String = "",
    @SerializedName("asn")
    val asn: Int? = null,
    @SerializedName("asOrganization")
    val asOrganization: String? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("countryCode")
    val countryCode: String? = null,
    @SerializedName("region")
    val region: String? = null,
    @SerializedName("regionCode")
    val regionCode: String? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("timezone")
    val timezone: String? = null,
    @SerializedName("longitude")
    val longitude: String? = null,
    @SerializedName("latitude")
    val latitude: String? = null,
    @SerializedName("postalCode")
    val postalCode: String? = null,
    @SerializedName("fraudScore")
    val fraudScore: Int? = null,
    @SerializedName("isResidential")
    val isResidential: Boolean? = null,
    @SerializedName("isBroadcast")
    val isBroadcast: Boolean? = null,
    @SerializedName("userAgent")
    val userAgent: String? = null
)

/**
 * VPN连接状态
 */
enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

/**
 * 代理模式
 */
enum class ProxyMode {
    GLOBAL,     // 全局代理
    SMART       // 智能分流（国内直连，国外代理）
}
