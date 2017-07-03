package com.ywtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.mobileim.IYWLoginService;
import com.alibaba.mobileim.IYWP2PPushListener;
import com.alibaba.mobileim.IYWTribePushListener;
import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWLoginParam;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.contact.IYWContactHeadClickCallback;
import com.alibaba.mobileim.contact.IYWContactService;
import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.YWCustomMessageBody;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeType;
import com.alibaba.mobileim.login.IYWConnectionListener;
import com.alibaba.mobileim.login.YWLoginCode;
import com.alibaba.mobileim.utility.IMNotificationUtils;
import com.ywtest.group.TribeSampleHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 连接状态监听
     */
    private IYWConnectionListener mConnectionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_chatting).setOnClickListener(this);
        findViewById(R.id.btn_Conversation).setOnClickListener(this);
        findViewById(R.id.btn_create_group).setOnClickListener(this);
        findViewById(R.id.btn_out).setOnClickListener(this);
        addPushMessageListener();
        ywLogin();

        final IYWContactService contactService = Constant.mIMKit.getContactService();
        //头像点击的回调（开发者可以按需设置）
        contactService.setContactHeadClickCallback(new IYWContactHeadClickCallback() {
            @Override
            public Intent onShowProfileActivity(String userId, String appKey) {
                Toast.makeText(MainActivity.this, "点击了：" + userId, Toast.LENGTH_SHORT).show();
                return null;
            }

            @Override
            public Intent onDisposeProfileHeadClick(Context context, String s, String s1) {
                Log.e("tag","s=" + s + ",s1=" + s1);//s = 用户名账号 , s1 = appKey
                return null;
            }
        });
    }

    private void ywLogin() {
        //此实现不一定要放在Application onCreate中
        Constant.mIMKit = YWAPI.getIMKitInstance(Constant.user_login_name, MyApplication.YW_APP_KEY);
        IYWLoginService loginService = Constant.mIMKit.getLoginService();
        YWLoginParam loginParam = YWLoginParam.createLoginParam(Constant.user_login_name, Constant.user_login_password);
        loginService.login(loginParam, new IWxCallback() {
            @Override
            public void onSuccess(Object... objects) {
                Toast.makeText(MyApplication.getContext(),"登录成功",Toast.LENGTH_SHORT).show();
                mConnectionListener = new IYWConnectionListener() {
                    @Override
                    public void onDisconnect(int code, String info) {
                        if (code == YWLoginCode.LOGON_FAIL_KICKOFF) {
                            //在其它终端登录，当前用户被踢下线
                            Toast.makeText(MyApplication.getContext(),"下线了",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onReConnecting() {

                    }

                    @Override
                    public void onReConnected() {

                    }
                };
                //注册连接状态监听
                Constant.mIMKit.getIMCore().addConnectionListener(mConnectionListener);
            }

            @Override
            public void onError(int i, String s) {
                Log.e("tag","登录失败" + s);
            }

            @Override
            public void onProgress(int i) {

            }
        });
    }

    /**
     * 登出
     */
    public void loginOut_Sample() {
        if (Constant.mIMKit == null) {
            return;
        }
        // openIM SDK提供的登录服务
        IYWLoginService mLoginService = Constant.mIMKit.getLoginService();
        mLoginService.logout(new IWxCallback() {

            @Override
            public void onSuccess(Object... arg0) {
                Toast.makeText(MainActivity.this, "退出成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onProgress(int arg0) {

            }

            @Override
            public void onError(int arg0, String arg1) {
                Toast.makeText(MainActivity.this, "退出失败" + arg1, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 添加新消息到达监听，该监听应该在登录之前调用以保证登录后可以及时收到消息
     */
    private void addPushMessageListener(){
        if (Constant.mIMKit == null) {
            return;
        }

        IYWConversationService conversationService = Constant.mIMKit.getConversationService();
        //添加单聊消息监听，先删除再添加，以免多次添加该监听
        conversationService.removeP2PPushListener(mP2PListener);
        conversationService.addP2PPushListener(mP2PListener);

        //添加群聊消息监听，先删除再添加，以免多次添加该监听
        conversationService.removeTribePushListener(mTribeListener);
        conversationService.addTribePushListener(mTribeListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_chatting:
                final String target = "testpro1"; //消息接收者ID
                final String appkey = MyApplication.YW_APP_KEY; //消息接收者appKey
                Intent intent = Constant.mIMKit.getChattingActivityIntent(target, appkey);
                startActivity(intent);
                break;
            case R.id.btn_Conversation://打开最近联系人界面
                Intent intent2 = Constant.mIMKit.getConversationActivityIntent();
                startActivity(intent2);
                break;
            case R.id.btn_create_group://创建群
                TribeSampleHelper.createTribe_Sample(YWTribeType.CHATTING_TRIBE,"ts_create");//YWTribeType.CHATTING_GROUP为讨论组
                break;
            case R.id.btn_out:
                loginOut_Sample();
                break;
        }
    }


    /**
     * 单聊消息监听(收到自定义消息时的回调)
     */
    private IYWP2PPushListener mP2PListener = new IYWP2PPushListener() {
        @Override
        public void onPushMessage(IYWContact contact, List<YWMessage> messages) {
            //NotifyMsg.toSystemNotifyPlugin("数据");
            for (YWMessage message : messages) {
                if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_P2P_CUS) {
                    if (message.getMessageBody() instanceof YWCustomMessageBody) {
                        YWCustomMessageBody messageBody = (YWCustomMessageBody) message.getMessageBody();
                        if (messageBody.getTransparentFlag() == 1) {
                            String content = messageBody.getContent();
                            try {
                                JSONObject object = new JSONObject(content);
                                if (object.has("text")) {
                                    String text = object.getString("text");
                                    IMNotificationUtils.getInstance().showToast(MyApplication.getContext(), "透传消息，content = " + text);
                                } else if (object.has("customizeMessageType")) {
                                    String customType = object.getString("customizeMessageType");
                                    Log.e("tag","自定义的消息类型");
                                    //自定义的消息类型
                                    /*if (!TextUtils.isEmpty(customType) && customType.equals(ChattingOperationCustomSample.CustomMessageType.READ_STATUS)) {
                                        YWConversation conversation = Constant.mIMKit.getConversationService().getConversationByConversationId(message.getConversationId());
                                        long msgId = Long.parseLong(object.getString("PrivateImageRecvReadMessageId"));
                                        conversation.updateMessageReadStatus(conversation, msgId);
                                    }*/
                                }
                            } catch (JSONException e) {
                            }
                        }
                    }
                }
            }
        }
    };

    /**
     * 群聊消息监听
     */
    private IYWTribePushListener mTribeListener = new IYWTribePushListener() {
        @Override
        public void onPushMessage(YWTribe tribe, List<YWMessage> messages) {
            //TODO 收到群消息
        }
    };
}
