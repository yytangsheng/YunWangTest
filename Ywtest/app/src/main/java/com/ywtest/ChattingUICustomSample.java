package com.ywtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.YWChannel;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMChattingPageUI;
import com.alibaba.mobileim.channel.util.AccountUtils;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.conversation.YWP2PConversationBody;
import com.alibaba.mobileim.conversation.YWTribeConversationBody;
import com.alibaba.mobileim.utility.IMNotificationUtils;

import static com.ywtest.Constant.mIMKit;

/**
 * 聊天界面自带提供两种主题的自定义供用户方便的使用，用户可以通过{@link ｝中 实现 AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_UI_POINTCUT, ChattingUICustomSample.class);
 * 使用该主题的聊天界面自定义风格：文字和图片小猪气泡风格
 *
 * todo 聊天界面的自定义风格1：文字和图片小猪气泡风格
 * Created by mayongge on 15-9-23.
 */
public class ChattingUICustomSample extends IMChattingPageUI {

    public ChattingUICustomSample(Pointcut pointcut) {
        super(pointcut);
    }


    @Override
    public View getCustomTitleView(final Fragment fragment, final Context context, LayoutInflater inflater, final YWConversation conversation) {
        // 单聊和群聊都会使用这个方法，所以这里需要做一下区分
        // 本demo示例是处理单聊，如果群聊界面也支持自定义，请去掉此判断

        //TODO 重要：必须以该形式初始化view---［inflate(R.layout.**, new RelativeLayout(context),false)］------，以让inflater知道父布局的类型，否则布局**中的高度和宽度无效，均变为wrap_content
        View view = inflater.inflate(R.layout.demo_custom_chatting_title, new RelativeLayout(context), false);
        view.setBackgroundColor(Color.parseColor("#00b4ff"));
        TextView textView = (TextView) view.findViewById(R.id.title);
        String title = null;
        if (conversation.getConversationType() == YWConversationType.P2P) {
            YWP2PConversationBody conversationBody = (YWP2PConversationBody) conversation
                    .getConversationBody();
            if (!TextUtils.isEmpty(conversationBody.getContact().getShowName())) {
                title = conversationBody.getContact().getShowName();
            } else {

                YWIMKit imKit = mIMKit;
                IYWContact contact = imKit.getContactService().getContactProfileInfo(conversationBody.getContact().getUserId(), conversationBody.getContact().getAppKey());
                //生成showName，According to id。
                if (contact != null && !TextUtils.isEmpty(contact.getShowName())) {
                    title = contact.getShowName();
                }
            }
            //如果标题为空，那么直接使用Id
            if (TextUtils.isEmpty(title)) {
                title = conversationBody.getContact().getUserId();
            }
        } else {
            if (conversation.getConversationBody() instanceof YWTribeConversationBody) {
                title = ((YWTribeConversationBody) conversation.getConversationBody()).getTribe().getTribeName();
                if (TextUtils.isEmpty(title)) {
                    title = "自定义的群标题";
                }
            } else {
                if (conversation.getConversationType() == YWConversationType.SHOP) { //为OpenIM的官方客服特殊定义了下、
                    title = AccountUtils.getShortUserID(conversation.getConversationId());
                }
            }
        }
        textView.setText(title);
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        textView.setTextSize(15);
        TextView backView = (TextView) view.findViewById(R.id.back);
        backView.setTextColor(Color.parseColor("#FFFFFF"));
        backView.setTextSize(15);
        //backView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.demo_common_back_btn_white, 0, 0, 0);
        backView.setGravity(Gravity.CENTER_VERTICAL);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                fragment.getActivity().finish();
            }
        });

        ImageView btn = (ImageView) view.findViewById(R.id.title_button);
        if (conversation.getConversationType() == YWConversationType.Tribe) {
            btn.setImageResource(R.drawable.aliwx_tribe_info_icon);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//群右边按钮单机事件
                    String conversationId = conversation.getConversationId();
                    long tribeId = Long.parseLong(conversationId.substring(5));
                    Log.e("tag","群名称=" + mIMKit.getTribeService().getTribe(tribeId).getTribeName());
                    Toast.makeText(context, "群id=" + tribeId, Toast.LENGTH_SHORT).show();
                }
            });
            btn.setVisibility(View.VISIBLE);
        } else if (conversation.getConversationType() == YWConversationType.P2P) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    YWP2PConversationBody pConversationBody = (YWP2PConversationBody) conversation.getConversationBody();
                    String appKey = pConversationBody.getContact().getAppKey();
                    String userId = pConversationBody.getContact().getUserId();
                    Toast.makeText(context, "用户id=" + userId, Toast.LENGTH_SHORT).show();
                }
            });
            btn.setVisibility(View.VISIBLE);

            /*String feedbackAccount = IMPrefsTools.getStringPrefs(IMChannel.getApplication(), IMPrefsTools.FEEDBACK_ACCOUNT, "");
            if (!TextUtils.isEmpty(feedbackAccount) && feedbackAccount.equals(AccountUtils.getShortUserID(conversation.getConversationId()))) {
                btn.setVisibility(View.GONE);
            }*/
        }
        return view;
    }

    @Override
    public void onItemClick(IYWContact loginAccount, YWMessage message, Bitmap bitmap, String item) {
        IMNotificationUtils.getInstance().showToast(item, YWChannel.getApplication());
    }
}
