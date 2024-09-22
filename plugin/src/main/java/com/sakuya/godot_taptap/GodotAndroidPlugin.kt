package com.sakuya.godot_taptap

import android.util.Log
import android.widget.Toast
import com.sakuya.godot_taptap.taptap.GodotTapTap
import com.sakuya.godot_taptap.taptap.TapAdNativeHelper
import com.taptap.sdk.compilance.TapTapCompliance
import com.taptap.sdk.compilance.TapTapComplianceCallback
import com.taptap.sdk.moment.TapTapMoment

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

class GodotAndroidPlugin(godot: Godot): GodotPlugin(godot) {

    private var godotTapTap:GodotTapTap ?= null

    override fun getPluginName() = "GodotTapTapSDK"


    override fun getPluginSignals(): MutableSet<SignalInfo> {
        return mutableSetOf(
            SignalInfo("onLoginResult",Integer::class.java,String::class.java),
            SignalInfo("onAntiAddictionCallback",Integer::class.java),
            SignalInfo("onTapMomentCallBack",Integer::class.java,String::class.java),
            SignalInfo("onRewardVideoAdCallBack",Integer::class.java)
        )
    }

    @UsedByGodot
    private fun test(){
        runOnUiThread {
            Toast.makeText(activity,"test",Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 初始化方法
     */
    @UsedByGodot
    private fun init(clientId:String?,clientToken:String?){
        godotTapTap = GodotTapTap.build {
            this.clientId = clientId
            this.clientToken = clientToken
            // this.serverUrl = serverUrl
        }
        activity?.let {

            godotTapTap?.init(it) {
                TapTapCompliance.registerComplianceCallback(
                    callback = object : TapTapComplianceCallback {

                        override fun onComplianceResult(code: Int, extra: Map<String, Any>?) {
                            // code == 500;   // 登录成功
                            // code == 1000;  // 用户登出
                            // code == 1001;  // 切换账号
                            // code == 1030;  // 用户当前无法进行游戏
                            // code == 1050;  // 时长限制
                            // code == 9002;  // 实名过程中点击了关闭实名窗
                            emitSignal("onAntiAddictionCallback", code)
                        }
                    }
                )
            }

                TapTapMoment.setCallback(
                    callback = object : TapTapMoment.TapTapMomentCallback {
                        override fun onCallback(code: Int, msg: String?) {
                            // 根据 code 处理事件
                            emitSignal("onTapMomentCallBack",code,msg)
                        }
                    }
                )
            }
        }

    /**
     * 是否登录
     */
    @UsedByGodot
    private fun isLogin():Boolean{
        return if ( godotTapTap != null)  godotTapTap!!.isLogin() else false
    }

    /**
     * 一键登录
     */
    @UsedByGodot
    private fun login(){
        activity?.let { godotTapTap?.login(it){
            if (it == null){
                emitSignal("onLoginResult",400,"")
            }else{
                emitSignal("onLoginResult",200,it)
            }
        } }
    }

    /**
     * 获取当前玩登录用户信息
     */
    @UsedByGodot
    private fun getCurrentProfile():String?{
        return godotTapTap?.getCurrentProfile()
    }

    /**
     * 登出
     */
    @UsedByGodot
    private fun logOut(){
        godotTapTap?.logOut()
    }

    /**
     * 防沉迷快速认证
     */
    @UsedByGodot
    private fun quickCheck(userIdentifier:String){
        activity?.let { TapTapCompliance.startup(it, userIdentifier) }
    }

    /**
     * 防沉迷登出
     */
    @UsedByGodot
    private fun antiExit(){
        TapTapCompliance.exit();
    }

    /**
     * 打开内嵌动态
     */
    @UsedByGodot
    private fun momentOpen(){
        godotTapTap?.momentOpen()
    }

    /**
     * adn初始化
     */
    @UsedByGodot
    private fun adnInit(mediaId:Long,mediaName:String,mediaKey:String){
        activity?.let {
            godotTapTap?.adnInit(it,mediaId,mediaName,mediaKey)
        }
    }

    /**
     * 激励广告初始化
     */
    @UsedByGodot
    private fun initRewardVideoAd(spaceId:Int,rewardName:String,extraInfo:String,userId:String){
        activity?.let {
            TapAdNativeHelper.initAd(it,spaceId,rewardName,extraInfo,userId){code ->
                emitSignal("onRewardVideoAdCallBack",code)
            }
        }
    }

    /**
     * 展示激励广告
     */
    @UsedByGodot
    private fun showRewardVideoAd(){
        activity?.let {
            TapAdNativeHelper.showAd(it)
        }
    }
}