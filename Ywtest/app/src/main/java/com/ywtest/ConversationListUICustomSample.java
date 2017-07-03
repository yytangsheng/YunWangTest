package com.ywtest;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMConversationListUI;
import com.alibaba.mobileim.channel.util.YWLog;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.mobileim.utility.IMSmilyCache;
import com.alibaba.mobileim.utility.IMUtil;

import java.util.HashMap;
import java.util.Map;

import static com.ywtest.ChattingOperationCustomSample.type_1;
import static com.ywtest.Constant.mIMKit;

/**
 * 会话列表
 */

public class ConversationListUICustomSample extends IMConversationListUI {

    private static final String TAG = "ConversationListUICustomSample";

    public ConversationListUICustomSample(Pointcut pointcut) {
        super(pointcut);
    }

    /**
     * 返回会话列表页面自定义标题
     * @param fragment
     * @param context
     * @param inflater
     * @return
     */
    @Override
    public View getCustomConversationListTitle(Fragment fragment, final Context context, LayoutInflater inflater) {
        //TODO 重要：必须以该形式初始化customView---［inflate(R.layout.**, new RelativeLayout(context),false)］------，以让inflater知道父布局的类型，否则布局xml**中定义的高度和宽度无效，均被默认的wrap_content替代
        RelativeLayout customView = (RelativeLayout) inflater
                .inflate(R.layout.demo_custom_contacts_title_bar, new RelativeLayout(context),false);
        customView.findViewById(R.id.title_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //订单
                Toast.makeText(context, "点击了右边的按钮", Toast.LENGTH_SHORT).show();
            }
        });
        return customView;
    }

    @Override
    public boolean needHideNullNetWarn(Fragment fragment) {
        return false;//不隐藏无网络提醒
    }

    @Override
    public boolean enableSearchConversations(Fragment fragment){
        return true;//支持会话列表搜索功能
    }

    /**
     * 该方法可以构造一个会话列表为空时的展示View
     * @return
     *      empty view
     */
    @Override
    public View getCustomEmptyViewInConversationUI(Context context) {
        /** 以下为示例代码，开发者可以按需返回任何view*/
        TextView textView = new TextView(context);
        textView.setText("还没有会话哦，快去找人聊聊吧!");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        return textView;
    }



    /*********** 以下是定制会话item view的示例代码 ***********/
    //有几种自定义，数组元素就需要几个，数组元素值从0开始
    //private final int[] viewTypeArray = {0,1,2,3}，这样就有4种自定义View
    private final int[] viewTypeArray = {0};
    /**
     * 自定义item view的种类数
     * @return 种类数
     */
    @Override
    public int getCustomItemViewTypeCount() {
        return viewTypeArray.length;
    }

    /**
     * 自定义item的viewType
     * @param conversation
     * @return
     */
    @Override
    public int getCustomItemViewType(YWConversation conversation) {
        //todo 若修改 YWConversationType.Tribe为自己type，SDK认为您要在｛@link #getCustomItemView｝中完全自定义，针对群的自定义，如getTribeConversationHead会失效。
        //todo 该原则同样适用于 YWConversationType.P2P等其它内部类型，请知晓！
        if (conversation.getLastestMessage().getSubType() == YWMessage.SUB_MSG_TYPE.IM_P2P_CUS || conversation.getLastestMessage().getSubType() == YWMessage.SUB_MSG_TYPE.IM_TRIBE_CUS) {
            if (conversation.getLastestMessage().getCustomMsgSubType() == type_1) {
                //点击的自定义消息
                return viewTypeArray[0];
            }
        }
        //这里必须调用基类方法返回！！
        return super.getCustomItemViewType(conversation);
    }



    /**
     * 根据viewType自定义item的view
     * @param fragment
     * @param conversation      当前item对应的会话
     * @param convertView       convertView
     * @param viewType          当前itemView的viewType
     * @param headLoadHelper    加载头像管理器，用户可以使用该管理器设置头像
     * @param parent            getView中的ViewGroup参数
     * @return
     */
    @Override
    public View getCustomItemView(Fragment fragment, YWConversation conversation, View convertView, int viewType, YWContactHeadLoadHelper headLoadHelper, ViewGroup parent) {
        if (viewType == viewTypeArray[0]){
            ViewHolder2 holder = null;
            if (convertView == null){
                LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
                holder = new ViewHolder2();
                convertView = inflater.inflate(R.layout.demo_custom_conversation_item_2, parent, false);
                holder.head = (ImageView) convertView.findViewById(R.id.head);
                holder.unread = (TextView) convertView.findViewById(R.id.unread);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.atMsgNotify = (TextView) convertView.findViewById(R.id.at_msg_notify);
                holder.draftNotify = (TextView) convertView.findViewById(R.id.at_msg_notify);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(holder);
                YWLog.i(TAG, "convertView == null");
            } else {
                holder = (ViewHolder2)convertView.getTag();
                YWLog.i(TAG, "convertView != null");
            }

            holder.unread.setVisibility(View.GONE);
            int unreadCount = conversation.getUnreadCount();
            if (unreadCount > 0) {
                holder.unread.setVisibility(View.VISIBLE);
                if (unreadCount > 99){
                    holder.unread.setText("99+");
                }else {
                    holder.unread.setText(String.valueOf(unreadCount));
                }
            }
            headLoadHelper.setHeadView(holder.head, conversation);
            if(conversation.getConversationType() == YWConversationType.P2P){
                holder.name.setText(conversation.getLastestMessage().getAuthorUserId());//显示发送者的用户名
            }else {
                String conversationId = conversation.getConversationId();
                long tribeId = Long.parseLong(conversationId.substring(5));
                holder.name.setText(mIMKit.getTribeService().getTribe(tribeId).getTribeName());//显示群名称
            }

            //是否支持群@消息提醒
            boolean isAtEnalbe = YWAPI.getYWSDKGlobalConfig().enableTheTribeAtRelatedCharacteristic();
            if (isAtEnalbe){
                //文案修改为@消息提醒
                holder.atMsgNotify.setText(R.string.aliwx_at_msg_notify);
                if (conversation.hasUnreadAtMsg()) {
                    holder.atMsgNotify.setVisibility(View.VISIBLE);
                } else {
                    holder.atMsgNotify.setVisibility(View.GONE);
                }
            } else {
                holder.atMsgNotify.setVisibility(View.GONE);
            }
            //String content = conversation.getLatestContent();
            String content = conversation.getLastestMessage().getMessageBody().getContent();
            boolean isDraftEnable = YWAPI.getYWSDKGlobalConfig().enableConversationDraft();
            //没有开启@消息开关或者@消息提醒不可见,说明此时没有@消息,检查是否有草稿
            if(isDraftEnable) {
                if (!isAtEnalbe || (holder.atMsgNotify != null && holder.atMsgNotify.getVisibility() != View.VISIBLE)) {
                    if (conversation.getConversationDraft() != null
                            && !TextUtils.isEmpty(conversation.getConversationDraft().getContent())) {
                        //文案修改为草稿提醒
                        holder.draftNotify.setText(R.string.aliwx_draft_notify);
                        content = conversation.getConversationDraft().getContent();
                        holder.draftNotify.setVisibility(View.VISIBLE);
                    } else {
                        holder.draftNotify.setVisibility(View.GONE);
                    }
                }
            }
            holder.content.setText(content);
            setSmilyContent(fragment.getActivity(), content, holder);
            holder.time.setText(IMUtil.getFormatTime(conversation.getLatestTimeInMillisecond(), mIMKit.getIMCore().getServerTime()));
            return convertView;
        }

        return super.getCustomItemView(fragment, conversation, convertView, viewType, headLoadHelper, parent);
    }
    private Map<String, CharSequence> mSmilyContentCache = new HashMap<String, CharSequence>();  //表情的本地缓存，加速读取速度用
    IMSmilyCache smilyManager;
    int defaultSmilySize = 0;
    private int contentWidth;

    private void setSmilyContent(Context context, String content, ViewHolder2 holder){
        initSmilyManager(context);
        if (content == null || holder.content.getPaint() == null) {
            CharSequence charSequence = mSmilyContentCache.get(content);
            if (charSequence != null) {
                holder.content.setText(charSequence);
            } else {
                CharSequence smilySpanStr = smilyManager.getSmilySpan(context,
                        content, defaultSmilySize, false);
                mSmilyContentCache.put(content, smilySpanStr);
                holder.content.setText(smilySpanStr);
            }
        } else {
            CharSequence charSequence = mSmilyContentCache.get(content);
            if (charSequence != null) {
                holder.content.setText(charSequence);
            } else {
                CharSequence text = TextUtils.ellipsize(content,
                        holder.content.getPaint(), contentWidth,
                        holder.content.getEllipsize());
                CharSequence smilySpanStr = smilyManager.getSmilySpan(context,
                        String.valueOf(text), defaultSmilySize, false);
                mSmilyContentCache.put(content, smilySpanStr);
                holder.content.setText(smilySpanStr);
            }
        }
    }

    private void initSmilyManager(Context context){
        if (smilyManager == null){
            smilyManager = IMSmilyCache.getInstance();
            defaultSmilySize = (int) context.getResources().getDimension(R.dimen.aliwx_smily_column_width);
            int width = context.getResources().getDisplayMetrics().widthPixels;
            contentWidth = width
                    - context.getResources().getDimensionPixelSize(R.dimen.aliwx_column_up_unit_margin)*2
                    - context.getResources().getDimensionPixelSize(R.dimen.aliwx_common_head_size)
                    - context.getResources().getDimensionPixelSize(R.dimen.aliwx_message_content_margin_right);
        }
    }


    public class ViewHolder2{
        ImageView head;
        TextView unread;
        TextView name;
        TextView content;
        TextView atMsgNotify;
        TextView draftNotify;
        TextView time;
    }

}
