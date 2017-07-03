package com.ywtest;

import android.content.Intent;

import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMNotification;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.utility.IMSmilyCache;

/**
 * 通知栏的一些自定义设置
 * @author zhaoxu
 *
 */
public class NotificationInitSampleHelper extends IMNotification{

    //免打扰模式
    private static boolean mNeedQuiet;

    //需要震动
    private static boolean mNeedVibrator = true;

    //需要声音
    private static boolean mNeedSound = true;

    public NotificationInitSampleHelper(Pointcut pointcut) {
        super(pointcut);
    }

    public void setNeedQuiet(boolean needQuiet){
        mNeedQuiet = needQuiet;
    }

    public void setNeedVibrator(boolean needVibrator){
        mNeedVibrator = needVibrator;
    }

    public void setNeedSound(boolean needSound){
        mNeedSound = needSound;
    }

    /**
     * 是否开启免打扰模式，若开启免打扰模式则收到新消息时不发送通知栏提醒，只在会话列表页面显示未读数
     * 若开启免打扰模式，则声音提醒和震动提醒会失效，即收到消息时不会有震动和提示音
     * @param conversation 会话id
     * @param message 收到的消息
     * @return true:开启， false：不开启
     */
    @Override
    public boolean needQuiet(YWConversation conversation, YWMessage message) {
//        if (conversation.getConversationType() == YWConversationType.Tribe){
//            return true;
//        }
        return mNeedQuiet;
    }

    /**
     * 收到通知栏消息时是否震动提醒，该设置在没有开启免打扰模式的情况下才有效
     * @param conversation 会话id
     * @param message 收到的消息
     * @return true：震动，false：不震动
     */
    @Override
    public boolean needVibrator(YWConversation conversation, YWMessage message) {
//        if (conversation.getConversationType() == YWConversationType.Tribe){
//            return false;
//        }
        return mNeedVibrator;
    }


    /**
     * 收到通知栏消息时是否有声音提醒，该设置在没有开启免打扰模式的情况下才有效
     * @param conversation 会话id
     * @param message 收到的消息
     * @return true：有提示音，false：没有提示音
     */
    @Override
    public boolean needSound(YWConversation conversation, YWMessage message) {
//        if (conversation.getConversationType() == YWConversationType.Tribe){
//            return false;
//        }
        return mNeedSound;
    }

    /**
     * 收到消息时，自定义消息通知栏的提示文案
     * @param conversation
     * @param message
     * @param totalUnReadCount
     * @return，如果返回空，则使SDK默认的文案格式
     */
    @Override
    public CharSequence getNotificationTips(YWConversation conversation, YWMessage message, int totalUnReadCount, IMSmilyCache smilyCache) {
//        int bound = (int) DemoApplication.getContext().getResources().getDimension(
//                com.alibaba.sdk.android.R.dimen.aliwx_smily_column_width);
//        CharSequence charSequence= smilyCache.getSmilySpan(DemoApplication.getContext(), message.getContent(), bound, false);
//        return charSequence.toString();
        return null;
    }

    /**
     * 收到消息时的自定义通知栏点击Intent
     * @param conversation
     *          收到消息的会话
     * @param message
     *          收到的消息
     * @param totalUnReadCount
     *          会话中消息未读数
     * @return
     *          如果返回null，则使用全局自定义Intent
     */
    public Intent getCustomNotificationIntent(YWConversation conversation, YWMessage message, int totalUnReadCount) {
        //以下仅为示例代码，需要Intent开发者根据不同目的自己实现
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        intent.setClass(DemoApplication.getContext(), FragmentTabs.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        return intent;

        return null;
    }

    /**
     * 获取通知栏图标Icon
     * @return ResId
     */
    @Override
    public int getNotificationIconResID() {
        return R.drawable.aliwx_notification_bg;
    }

    /**
     * 获取通知栏显示Title
     * @return  显示app名字
     */
    @Override
    public String getAppName() {
        return "我的OpenIM";
    }

    /**
     * 返回自定义提示音资源Id
     * @return  提示音资源Id，返回0则使用SDK默认的提示音
     */
    @Override
    public int getNotificationSoundResId() {
        return 0;
    }


    /**
     * 自定义通知栏ticker
     * @param conversation
     *          收到消息的会话
     * @param message
     *          收到的消息
     * @param totalUnReadCount
     *          会话中消息未读数
     * @return
     *          如果返回null，则使用SDK默认的ticker
     */
    @Override
    public String getTicker(YWConversation conversation, YWMessage message, int totalUnReadCount) {
        //return "123456"; 代表通知栏下面的显示内容
        return null;
    }

    /**
     * 收到自定义消息时的提示文案
     * @param conversation
     * @param message
     * @param totalUnReadCount
     * @return
     */
    @Override
    public String getNotificationTips(YWConversation conversation, YWMessage message, int totalUnReadCount) {
        if(message.getCustomMsgSubType() == ChattingOperationCustomSample.type_1){
            if(totalUnReadCount == 1){
                return message.getMessageBody().getContent();
            }else{
                return "您有" + totalUnReadCount + "条未读消息";
            }
        }
        return super.getNotificationTips(conversation, message, totalUnReadCount);
    }
}
