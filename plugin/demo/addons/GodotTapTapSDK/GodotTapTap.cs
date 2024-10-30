using Godot;
using System;

public partial class GodotTapTap : Node
{
	[Signal]
	public delegate void OnLoginResultEventHandler(int code, string json);
	[Signal]
	public delegate void OnAntiAddictionCallackEventHandler(int code);
	[Signal]
	public delegate void OnTapMomentCallbackEventHandler(int code);
	[Signal]
	public delegate void OnRewardVideoAdCallbackEventHandler(int code);

	public static GodotTapTap Instance;
	[Export]
	public GodotObject Singleton;

	public override void _Ready()
	{
		Instance = this;
		Singleton = (GodotObject)GetNode("/root/GodotTapTap").Get("singleton");
		Singleton.Connect("onLoginResult", Callable.From((int code, string json) => _OnLoginResult(code, json)));
		Singleton.Connect("onAntiAddictionCallback", Callable.From((int code) => _OnAntiAddictionCallback(code)));
		Singleton.Connect("onTapMomentCallBack", Callable.From((int code, string msg) => _OnTapMomentCallback(code, msg)));
		Singleton.Connect("onRewardVideoAdCallBack", Callable.From((int code) => _OnRewardVideoAdCallback(code)));
	}

	private void _OnLoginResult(int code, string json)
	{
		EmitSignal(SignalName.OnLoginResult, code, json);
	}

	private void _OnAntiAddictionCallback(int code)
	{
		EmitSignal(SignalName.OnAntiAddictionCallack, code);
	}

	private void _OnTapMomentCallback(int code, string msg)
	{
		EmitSignal(SignalName.OnTapMomentCallback, code, msg);
	}

	private void _OnRewardVideoAdCallback(int code)
	{
		EmitSignal(SignalName.OnRewardVideoAdCallback, code);
	}

	public void TapLogin()
	{
		Singleton.Call("login");
	}

	public bool IsLogin()
	{
		return (bool)Singleton.Call("isLogin");
	}

	public string GetCurrentProfile()
	{
		return (string)Singleton.Call("getCurrentProfile");
	}

	public void LogOut()
	{
		Singleton.Call("logOut");
	}

	public void QuickCheck(string id = null)
	{
		if (id is null)
		{
			id = OS.GetUniqueId();
		}
		Singleton.Call("quickCheck", id);
	}

	public void AntiExti()
	{
		Singleton.Call("antiExti");
	}

	public void MomentOpen()
	{
		Singleton.Call("momentOpen");
	}

	public void InitAd(long mediaId, string mediaName, string mediaKey)
	{
		Singleton.Call("adnInit", mediaId, mediaName, mediaKey);
	}

	public void InitRewardVideoAd(int spaceId, string rewardNamd, string extraInfo, string userId)
	{
		Singleton.Call("initRewardVideoAd", spaceId, rewardNamd, extraInfo, userId);
	}

	public void ShowRewardVideoAd()
	{
		Singleton.Call("showRewardVideoAd");
	}
}
