package xyz.a202132.app.util

import android.content.Context
import android.util.Log

/**
 * 原生签名验证，防止篡改的 APK 运行
 */
object SignatureVerifier {
    private const val TAG = "SignatureVerifier"

    init {
        try {
            System.loadLibrary("native-lib")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library", e)
        }
    }

    /**
     * 验证 APK 签名是否与预期哈希值相符
     * 如果验证失败，应用程序将崩溃（原生中止）
     */
    @JvmStatic
    external fun verifySignature(context: Context)
}
