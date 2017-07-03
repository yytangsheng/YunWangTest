package com.ywtest;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMChattingPageOperateion;
import com.alibaba.mobileim.aop.model.ReplyBarItem;
import com.alibaba.mobileim.aop.model.YWChattingPlugin;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWCustomMessageBody;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.conversation.YWMessageChannel;
import com.alibaba.mobileim.fundamental.widget.WxAlertDialog;
import com.alibaba.mobileim.lib.model.message.Message;
import com.alibaba.mobileim.utility.IMNotificationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天界面(单聊和群聊界面)的定制点(根据需要实现相应的接口来达到自定义聊天界面)，不设置则使用openIM默认的实现
 * 1.CustomChattingTitleAdvice 自定义聊天窗口标题 2. OnUrlClickChattingAdvice 自定义聊天窗口中
 * 当消息是url是点击的回调。用于isv处理url的打开处理。不处理则用第三方浏览器打开 如果需要定制更多功能，需要实现更多开放的接口
 * 需要.继承BaseAdvice .实现相应的接口
 * <p/>
 * 另外需要在Application中绑定
 * AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_POINTCUT,
 * ChattingOperationCustomSample.class);
 */
public class ChattingOperationCustomSample extends IMChattingPageOperateion {

    private static final String TAG = "ChattingOperationCustomSample";

    /** 自定义viewType，viewType的值必须从0开始，然后依次+1递增，且viewType的个数必须等于typeCount，切记切记！！！***/
    public final static int type_1 = 0;//自定义消息的类型，与下面的ITEM_ID_1对应

    //新增加的item
    private final static int ITEM_ID_0 = 0; //发送地图消息
    private final static int ITEM_ID_1 = 1; //用来发送自定义消息

    //*******************关于请求码的******************************
    private final static int request_code_map = 1; //跳转到地图界面的请求码

    /**
     * 保存点击新增item时发送消息的对象，便于在onActivityResult方法中使用
     */
    private YWConversation conversation;

    // 默认写法
    public ChattingOperationCustomSample(Pointcut pointcut) {
        super(pointcut);
    }


    /**
     * 第一步：用户创建自定义消息
     * @param type
     * @return
     */
    public static YWMessage createCustomMessage(YWConversationType type) {
        // 发送自定义消息
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
        // 请注意这里不一定要是JSON格式，这里纯粹是为了演示的需要
        messageBody.setContent("我是消息内容,你造不造");// 用户要发送的自定义消息，SDK不关心具体的格式，比如用户可以发送JSON格式
        messageBody.setSummary("消息摘要");// 可以理解为消息的标题，用于显示会话列表和消息通知栏
        // 注意，这里是群自定义消息
        if (type == YWConversationType.Tribe) {
            return YWMessageChannel.createTribeCustomMessage(messageBody);
        }
        // 注意，这里是单聊自定义消息
        return YWMessageChannel.createCustomMessage(messageBody);
    }

    /**
     * 第二步 创建显示自定义消息的布局
     * @param fragment
     * @param message
     * @return
     */
    @Override
    public View getCustomMessageView(Fragment fragment, YWMessage message) {
        String data = "";
        if (message.getMessageBody().getExtraData() != null) {
            // 这里的ExtraData主要是方便用户在内存中存储数据，这样有些复杂的消息就不需要反复地去解析
            data = (String) message.getMessageBody().getExtraData();
        } else {
            // 没有解析过，则解析一遍，然后临时存储到Extradata中
            data = message.getMessageBody().getContent();
            message.getMessageBody().setExtraData(data);
        }
        Log.e("tag","内容=" + data);
        //根据类型判断是哪种自定义消息
        if(message.getCustomMsgSubType() == type_1){
            LinearLayout layout = (LinearLayout) View.inflate(MyApplication.getContext(),R.layout.demo_custom_tribe_msg_layout, null);
            TextView textView = (TextView) layout.findViewById(R.id.msg_content);
            textView.setText(data);
            return layout;
        }
        return null;//空消息
    }

    /**
     *
     * 用于增加聊天窗口 下方回复栏的操作区的item
     *
     * ReplyBarItem
     * itemId:唯一标识 建议从1开始
     * ItemImageRes：显示的图片
     * ItemLabel：文字
     * needHide:是否隐藏 默认: false ,  显示：false ， 隐藏：true
     * OnClickListener: 自定义点击事件, null则使用默认的点击事件
     * 参照示例返回List<ReplyBarItem>用于操作区显示item列表，可以自定义顺序和添加item
     *
     * @param pointcut         聊天窗口fragment
     * @param conversation 当前会话，通过conversation.getConversationType() 区分个人单聊，与群聊天
     * @param replyBarItemList 默认的replyBarItemList，如拍照、选择照片、短视频等
     * @return
     */
    @Override
    public List<ReplyBarItem> getCustomReplyBarItemList(final Fragment pointcut,
                                                        final YWConversation conversation, List<ReplyBarItem> replyBarItemList) {
        List<ReplyBarItem> replyBarItems = new ArrayList<ReplyBarItem>();
        for (ReplyBarItem replyBarItem : replyBarItemList) {
            if(replyBarItem.getItemId()== YWChattingPlugin.ReplyBarItem.ID_CAMERA){
                //是否隐藏ReplyBarItem中的拍照选项
                replyBarItem.setNeedHide(false);
                //不自定义ReplyBarItem中的拍照的点击事件,设置OnClicklistener(null);
                replyBarItem.setOnClicklistener(null);
            }else if(replyBarItem.getItemId()== YWChattingPlugin.ReplyBarItem.ID_ALBUM){
                //是否隐藏ReplyBarItem中的选择照片选项
                replyBarItem.setNeedHide(false);
                //不自定义ReplyBarItem中的相册的点击事件,设置OnClicklistener（null）
                replyBarItem.setOnClicklistener(null);
            }else if(replyBarItem.getItemId()== YWChattingPlugin.ReplyBarItem.ID_SHORT_VIDEO){
                //默认配置是群聊时隐藏短视频按钮。这里是为了设置显示群聊短视频item
                if (conversation.getConversationType() == YWConversationType.Tribe){
                    replyBarItem.setNeedHide(false);
                }
            }
            replyBarItems.add(replyBarItem);
        }

        ReplyBarItem replyBarItem1 = new ReplyBarItem();
        replyBarItem1.setItemId(ITEM_ID_0);
        replyBarItem1.setItemImageRes(R.drawable.aliwx_s001);
        replyBarItem1.setItemLabel("item1");
        replyBarItems.add(replyBarItem1);

        ReplyBarItem replyBarItem2 = new ReplyBarItem();
        replyBarItem2.setItemId(ITEM_ID_1);
        replyBarItem2.setItemImageRes(R.drawable.aliwx_s002);
        replyBarItem2.setItemLabel("item2");
        replyBarItems.add(replyBarItem2);

        return replyBarItems;
    }

    /**
     * 当自定义的item点击时的回调
     * @param pointcut
     * @param item
     * @param conversation  保存发送消息的对象，在回调中发送消息
     */
    public void onReplyBarItemClick(Fragment pointcut,ReplyBarItem item,YWConversation conversation) {
        this.conversation = conversation;
        if(item.getItemId() == ITEM_ID_0){
            //这里用来发送地图消息
            Toast.makeText(pointcut.getActivity(), "点击了增加的item: id="+item.getItemId(), Toast.LENGTH_SHORT).show();
            //将会执行下面的onActivityResult方法
            pointcut.getActivity().startActivityForResult(new Intent(pointcut.getActivity(),Main2Activity.class),1);
        }else if(item.getItemId() == ITEM_ID_1){
            Toast.makeText(pointcut.getActivity(), "点击了增加的item: id="+item.getItemId(), Toast.LENGTH_SHORT).show();
            //用来发送自定义消息
            Log.e("tag","当前消息类型=" + conversation.getConversationType());//1为单聊  3为群聊
            if(conversation.getConversationType() == YWConversationType.P2P){
                //当前为单聊
                YWMessage customMessage = createCustomMessage(YWConversationType.P2P);//Tribe代表群聊
                customMessage.setContent("当前为单聊,我是自定义消息内容");
                customMessage.setCustomMsgSubType(type_1);//设置该自定义消息的类型
                conversation.getMessageSender().sendMessage(customMessage,120,null);
                Log.e("tag","发送单聊消息了");
            }else if(conversation.getConversationType() == YWConversationType.Tribe){
                YWMessage customMessage = createCustomMessage(YWConversationType.Tribe);//Tribe代表群聊
                customMessage.setContent("当前为群聊,我是自定义消息内容");
                customMessage.setCustomMsgSubType(type_1);//设置该自定义消息的类型
                conversation.getMessageSender().sendMessage(customMessage,120,null);
            }
        }
    }


    /*@Override //重写地图消息的布局
    public View getCustomGeoMessageView(Fragment fragment, YWMessage message) {
        YWGeoMessageBody messageBody = (YWGeoMessageBody)message.getMessageBody();
        String content = "纬度: " + messageBody.getLatitude() + ", 经度: " + messageBody.getLongitude() + ", 地址: " + messageBody.getAddress();
        LinearLayout layout = (LinearLayout) View.inflate(MyApplication.getContext(),
                R.layout.demo_custom_tribe_msg_layout, null);
        TextView textView = (TextView) layout.findViewById(R.id.msg_content);
        textView.setText(content);
        return layout;
    }*/

    /**
     * 发送地理位置消息
     */
    public static void sendGeoMessage(YWConversation conversation) {
        conversation.getMessageSender().sendMessage(
                YWMessageChannel.createGeoMessage(30.2743790000,
                        120.1422530000, "靠，这也行"), 120, null);
    }

    /**
     * 单聊ui界面，点击url的事件拦截 返回true;表示自定义处理，返回false，由默认处理
     *
     * @param fragment 可以通过 fragment.getActivity拿到Context
     * @param message  点击的url所属的message
     * @param url      点击的url
     */
    @Override
    public boolean onUrlClick(Fragment fragment, YWMessage message, String url,YWConversation conversation) {
        IMNotificationUtils.getInstance().showToast(fragment.getActivity(), "用户点击了url:" + url);
        if(!url.startsWith("http")) {
            url = "http://" + url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        fragment.startActivity(intent);
        return true;
    }

    /**
     * 如果开发者选择自己实现拍照或者选择照片的流程，则可以在该方法中实现照片(图片)的发送操作
     * @param requestCode
     * @param resultCode
     * @param data
     * @param messageList 开发者构造图片消息并赋值给message参数，sdk会把该消息发送出去
     * @return 开发者在自己实现拍照处理或者选择照片时，一定要return true
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data, List<YWMessage> messageList) {
        if(requestCode == request_code_map && resultCode == Activity.RESULT_OK){
            Toast.makeText(MyApplication.getContext(), "发送地图消息吧", Toast.LENGTH_SHORT).show();
            sendGeoMessage(conversation);
            return true;//代表自己处理了item点击后的处理
        }
        return false;
    }


    /**
     * 定制点击消息事件, 每一条消息的点击事件都会回调该方法，开发者根据消息类型，对不同类型的消息设置不同的点击事件
     * @param fragment  聊天窗口fragment对象
     * @param message   被点击的消息
     * @return true:使用用户自定义的消息点击事件，false：使用默认的消息点击事件
     */
    @Override
    public boolean onMessageClick(final Fragment fragment, final YWMessage message) {
        if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_TEXT){
            IMNotificationUtils.getInstance().showToast(fragment.getActivity(), "你点击了文本消息");
            return false;
        } else if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_GEO){
            Message mapMessage = (Message) message;
            mapMessage.getLatitude();//纬度
            mapMessage.getLongitude();
            mapMessage.getContent();
            Log.e("tag","纬度=" + mapMessage.getLatitude() + ",经度=" + mapMessage.getLongitude() + ",内容="+ mapMessage.getContent());
            IMNotificationUtils.getInstance().showToast(fragment.getActivity(), "内容="+ mapMessage.getContent());
            return false;
        }else if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_P2P_CUS || message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_TRIBE_CUS){
            if(message.getCustomMsgSubType() == type_1){
                //点击的自定义消息
                IMNotificationUtils.getInstance().showToast(fragment.getActivity(), "点击了自定义消息--类型为=" + type_1);
                return true;
            }
            return false;
        }
        return false;
    }



    /**
     * 数字字符串点击事件,开发者可以根据自己的需求定制
     * @param activity
     * @param clickString 被点击的数字string
     * @param widget 被点击的TextView
     * @return false:不处理
     *         true:需要开发者在return前添加自己实现的响应逻辑代码
     */
    @Override
    public boolean onNumberClick(final Activity activity, final String clickString, final View widget) {
        ArrayList<String> menuList = new ArrayList<String>();
        menuList.add("呼叫");
        menuList.add("添加到手机通讯录");
        menuList.add("复制到剪贴板");
        final String[] items = new String[menuList.size()];
        menuList.toArray(items);
        Dialog alertDialog = new WxAlertDialog.Builder(activity)
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (TextUtils.equals(items[which], "呼叫")) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + clickString));
                            activity.startActivity(intent);
                        } else if (TextUtils.equals(items[which], "添加到手机通讯录")) {
                            Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                            intent.setType("vnd.android.cursor.item/person");
                            intent.setType("vnd.android.cursor.item/contact");
                            intent.setType("vnd.android.cursor.item/raw_contact");
                            intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, clickString);
                            activity.startActivity(intent);

                        } else if (TextUtils.equals(items[which], "复制到剪贴板")) {
                            ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setText(clickString);
                        }
                    }
                }).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                widget.invalidate();
            }
        });
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
        return true;
    }

}