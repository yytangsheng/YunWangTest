package com.ywtest;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.aop.AdviceBinder;
import com.alibaba.mobileim.aop.PointCutEnum;
import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.media.MediaService;
import com.alibaba.wxlib.util.SysUtil;
import com.taobao.tae.sdk.callback.InitResultCallback;

/**
 * Created by huangyihui on 2017/5/31.
 */
public class MyApplication extends MultiDexApplication {

    private static final String TAG = "MyApplication";
    public static String YW_APP_KEY = "23015524";//  23879829

    //云旺OpenIM的DEMO用到的Application上下文实例
    private static Context sContext;
    public static Context getContext(){
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Application.onCreate中，首先执行这部分代码，以下代码固定在此处，不要改动，这里return是为了退出Application.onCreate！！！
        if(mustRunFirstInsideApplicationOnCreate()){
            //如果在":TCMSSevice"进程中，无需进行openIM和app业务的初始化，以节省内存
            return;
        }

        // 将MultiDex注入到项目中
        MultiDex.install(this);

        //初始化云旺SDK
        //第一个参数是Application Context
        //这里的APP_KEY即应用创建时申请的APP_KEY，同时初始化必须是在主进程中
        if(SysUtil.isMainProcess()){
            YWAPI.init(this, YW_APP_KEY);
        }

        //初始化多媒体SDK，小视频和阅后即焚功能需要使用多媒体SDK
        AlibabaSDK.asyncInit(this, new InitResultCallback() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "-----initTaeSDK----onSuccess()-------" );
                MediaService mediaService = AlibabaSDK.getService(MediaService.class);
                mediaService.enableHttpDNS(); //果用户为了避免域名劫持，可以启用HttpDNS
                mediaService.enableLog(); //在调试时，可以打印日志。正式上线前可以关闭
            }

            @Override
            public void onFailure(int code, String msg) {
                Log.e(TAG, "-------onFailure----msg:" + msg + "  code:" + code);
            }
        });

        //消息通知样式
        AdviceBinder.bindAdvice(PointCutEnum.NOTIFICATION_POINTCUT, NotificationInitSampleHelper.class);
        //自定义单聊与群聊标题头
        AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_UI_POINTCUT, ChattingUICustomSample.class);
        //自定义扩展消息类型
        AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_POINTCUT,ChattingOperationCustomSample.class);
        //会话列表
        AdviceBinder.bindAdvice(PointCutEnum.CONVERSATION_FRAGMENT_UI_POINTCUT, ConversationListUICustomSample.class);

    }



    private boolean mustRunFirstInsideApplicationOnCreate() {
        //必须的初始化
        SysUtil.setApplication(this);
        sContext = getApplicationContext();
        return SysUtil.isTCMSServiceProcess(sContext);
    }


}
