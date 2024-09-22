package com.sakuya.godot_taptap.taptap

import android.app.Activity
import android.content.res.TypedArray
import android.util.Log
import com.tapsdk.tapad.TapAdConfig
import com.tapsdk.tapad.TapAdCustomController
import com.tapsdk.tapad.TapAdManager
import com.tapsdk.tapad.TapAdSdk
import com.taptap.sdk.compilance.option.TapTapComplianceOptions
import com.taptap.sdk.core.TapTapLanguage
import com.taptap.sdk.core.TapTapRegion
import com.taptap.sdk.core.TapTapSdk
import com.taptap.sdk.core.TapTapSdkOptions
import com.taptap.sdk.kit.internal.callback.TapTapCallback
import com.taptap.sdk.kit.internal.exception.TapTapException
import com.taptap.sdk.kit.internal.extensions.toJson
import com.taptap.sdk.login.Scopes
import com.taptap.sdk.login.TapTapAccount
import com.taptap.sdk.login.TapTapLogin
import com.taptap.sdk.moment.TapTapMoment

class GodotTapTap(val clientId:String?,
                  val clientToken:String?) {

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var clientId: String? = null
        var clientToken: String? = null
        // var serverUrl: String? = null

        fun build() = GodotTapTap(clientId,clientToken)
    }

    /*
    初始化sdk
     */
    fun init(activity: Activity,block: () -> Unit){
        activity.runOnUiThread {
            TapTapSdk.init(
                activity,
                TapTapSdkOptions(
                    clientId.toString(), // 游戏 Client ID
                    clientToken.toString(), // 游戏 Client Token
                    TapTapRegion.CN, // 游戏可玩区域: [TapTapRegion.CN]=国内 [TapTapRegion.GLOBAL]=海外
                    "TapTap", // 分包渠道名称
                    "1.0", // 游戏版本号
                    false, // 是否自动上报 GooglePlay 内购支付成功事件 仅 [TapTapRegion.GLOBAL] 生效
                    false, // 自定义字段是否能覆盖内置字段
                    null, // 自定义属性，启动首个预置事件（device_login）会带上这些属性
                    null, // OAID 证书, 用于上报 OAID 仅 [TapTapRegion.CN] 生效
                    false, // 是否开启 log，建议 Debug 开启，Release 关闭，默认关闭 log
                    TapTapLanguage.AUTO, // TapSDK 首选语言 默认为 TapTapLanguage.AUTO
                ),
                options = arrayOf(
                // TapTapAchievementOptions(enableToast = true), // 成就初始化配置
                TapTapComplianceOptions(showSwitchAccount = true, useAgeRange = true) // 合规认证初始化配置
                )
            )

            block()
        }
    }

    fun adnInit(activity: Activity,mediaId:Long,mediaName:String,mediaKey:String){
        TapAdManager.get().requestPermissionIfNecessary(activity)
        val config = TapAdConfig.Builder()
            .withMediaId(mediaId) // 必选参数。为 TapADN 注册的媒体 ID
            .withMediaName(mediaName) // 必选参数。为 TapADN 注册的媒体名称
            .withMediaKey(mediaKey) // 必选参数。媒体密钥，可以在TapADN后台查看
            .withMediaVersion("1") // 必选参数。默认值 "1"
            .withTapClientId(clientId) // 可选参数。TapTap 开发者中心的游戏 Client ID
            .enableDebug(true) // 可选参数，是否打开 debug 调试信息输出：true 打开、false 关闭。默认 false 关闭
            .withGameChannel("TapTap") // 必选参数，渠道
            .withCustomController(object : TapAdCustomController() {

                // 开发者可以传入 oaid
                // 信通院 OAID 的相关采集——如何获取 OAID：
                // 1. 移动安全联盟官网 http://www.msa-alliance.cn/
                // 2. 信通院统一 SDK 下载 http://msa-alliance.cn/col.jsp?id=120
                override fun getDevOaid(): String {

                    return "aaa"
                }

                // 是否允许 SDK 主动获取 ANDROID_ID
                override fun isCanUseAndroidId(): Boolean {
                    return true
                }

            })
            .build()

        TapAdSdk.init(activity, config)
    }

    /*
    是否登录
     */
    fun isLogin():Boolean{
        return TapTapLogin.getCurrentTapAccount() != null
    }

    fun login(activity: Activity,block:(String?) -> Unit){
        val scopes = mutableSetOf<String>()
        scopes.add(Scopes.SCOPE_PROFILE)
        TapTapLogin.loginWithScopes(
            activity,
            scopes.toTypedArray(),
            object : TapTapCallback<TapTapAccount> {

                override fun onSuccess(result: TapTapAccount) {
                    // 成功
                    block(result.toJson())
                }

                override fun onCancel() {
                    // 取消
                    block(null)
                }

                override fun onFail(exception: TapTapException) {
                    // 失败
                    Log.e("tap_login error", exception.message.toString())
                    block(null)
                }
            }
        )
    }

    /*
    获取登录信息
     */
    fun getCurrentProfile():String?{
        return try {
            val profile: TapTapAccount? = TapTapLogin.getCurrentTapAccount()
            profile.toJson()
        }catch (e:Exception){
            null
        }
    }

    /**
     * 登出
     */
    fun logOut(){
        TapTapLogin.logout()
    }

    /**
     * 打开内嵌动态
     */
    fun momentOpen(){
        TapTapMoment.open()
    }
}