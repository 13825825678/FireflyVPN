package xyz.a202132.app.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 规则集管理器
 * 管理 sing-box rule_set 所需的 .srs 规则集文件
 * 
 * 流程：
 * 1. APP 启动时后台尝试下载最新规则集
 * 2. VPN 启动前调用 ensureRuleSets() 确保文件存在（从 assets 拷贝兜底）
 */
object RuleManager {
    private const val TAG = "RuleManager"

    // .srs 规则集下载地址 (jsDelivr CDN 加速)
    private const val GEOIP_CN_URL = "https://testingcf.jsdelivr.net/gh/SagerNet/sing-geoip@rule-set/geoip-cn.srs"
    private const val GEOSITE_CN_URL = "https://testingcf.jsdelivr.net/gh/SagerNet/sing-geosite@rule-set/geosite-cn.srs"

    // 文件名常量
    const val GEOIP_CN_FILE = "geoip-cn.srs"
    const val GEOSITE_CN_FILE = "geosite-cn.srs"

    /**
     * 确保规则集文件存在于 sing-box 工作目录中
     * 如果文件不存在或大小异常，从 assets 拷贝默认版本
     * 应在 VPN 启动前调用（同步，快速）
     */
    suspend fun ensureRuleSets(context: Context) {
        withContext(Dispatchers.IO) {
            val workDir = File(context.filesDir, "sing-box")
            if (!workDir.exists()) {
                workDir.mkdirs()
            }

            val geoipFile = File(workDir, GEOIP_CN_FILE)
            val geositeFile = File(workDir, GEOSITE_CN_FILE)

            if (!geoipFile.exists() || geoipFile.length() < 1024) {
                val ok = copyFromAssets(context, "rule-sets/$GEOIP_CN_FILE", geoipFile)
                if (!ok || !geoipFile.exists() || geoipFile.length() < 1024) {
                    throw IllegalStateException("Missing required rule set: $GEOIP_CN_FILE")
                }
            }

            if (!geositeFile.exists() || geositeFile.length() < 1024) {
                val ok = copyFromAssets(context, "rule-sets/$GEOSITE_CN_FILE", geositeFile)
                if (!ok || !geositeFile.exists() || geositeFile.length() < 1024) {
                    throw IllegalStateException("Missing required rule set: $GEOSITE_CN_FILE")
                }
            }
        }
    }

    /**
     * 后台下载/更新规则集文件
     * 下载成功则替换现有文件，失败则保留现有文件不影响使用
     * 应在 APP 启动时后台调用（异步，不阻塞）
     */
    suspend fun updateRuleSets(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val workDir = File(context.filesDir, "sing-box")
                if (!workDir.exists()) {
                    workDir.mkdirs()
                }

                val geoipFile = File(workDir, GEOIP_CN_FILE)
                val geositeFile = File(workDir, GEOSITE_CN_FILE)

                var allSuccess = true

                // 下载 geoip-cn.srs
                Log.i(TAG, "Updating $GEOIP_CN_FILE...")
                if (!downloadToFile(GEOIP_CN_URL, geoipFile)) {
                    Log.w(TAG, "Failed to update $GEOIP_CN_FILE, keeping existing file")
                    allSuccess = false
                }

                // 下载 geosite-cn.srs
                Log.i(TAG, "Updating $GEOSITE_CN_FILE...")
                if (!downloadToFile(GEOSITE_CN_URL, geositeFile)) {
                    Log.w(TAG, "Failed to update $GEOSITE_CN_FILE, keeping existing file")
                    allSuccess = false
                }

                if (allSuccess) {
                    Log.i(TAG, "All rule sets updated successfully")
                }
                allSuccess
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update rule sets", e)
                false
            }
        }
    }

    /**
     * 从 assets 拷贝默认规则集文件
     */
    private fun copyFromAssets(context: Context, assetPath: String, targetFile: File): Boolean {
        return try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.i(TAG, "Copied default ${targetFile.name} from assets (${targetFile.length()} bytes)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy ${targetFile.name} from assets", e)
            false
        }
    }

    /**
     * 下载文件到目标位置
     * 先下载到临时文件，成功后再替换，避免下载失败导致文件损坏
     */
    private fun downloadToFile(urlStr: String, targetFile: File): Boolean {
        val tempFile = File(targetFile.parent, "${targetFile.name}.tmp")
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.requestMethod = "GET"
            conn.connect()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }

                // 校验下载文件大小
                if (tempFile.length() < 1024) {
                    Log.e(TAG, "Downloaded file too small: ${tempFile.length()} bytes")
                    tempFile.delete()
                    return false
                }

                // 原子替换
                if (targetFile.exists() && !targetFile.delete()) {
                    Log.e(TAG, "Failed to delete old file: ${targetFile.absolutePath}")
                    tempFile.delete()
                    return false
                }
                if (!tempFile.renameTo(targetFile)) {
                    Log.e(TAG, "Failed to replace file: ${targetFile.absolutePath}")
                    tempFile.delete()
                    return false
                }
                Log.i(TAG, "Downloaded ${targetFile.name} successfully (${targetFile.length()} bytes)")
                return true
            } else {
                Log.e(TAG, "Download failed: HTTP ${conn.responseCode}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download error for $urlStr", e)
            tempFile.delete()
            return false
        } finally {
            conn?.disconnect()
        }
    }
}
