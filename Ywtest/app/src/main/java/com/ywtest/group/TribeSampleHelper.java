package com.ywtest.group;

import com.alibaba.mobileim.YWIMCore;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.channel.util.YWLog;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeMember;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeType;
import com.alibaba.mobileim.tribe.IYWTribeService;
import com.alibaba.mobileim.tribe.YWTribeCreationParam;
import com.ywtest.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * 群聊相关的API调用示例（部分API，主要是onSucces需要参数转换的一些API） 其它API的说明请参见官方的文档
 *
 *  * com.taobao.openimui.tribe    该目录是群功能示例代码，具体如下
 *  {@link com.taobao.openimui.tribe.EditMyTribeProfileActivity}
 *      修改我在本群的昵称
 *  {@link com.taobao.openimui.tribe.EditTribeInfoActivity}
 *      修改群名称、群公告
 *  {@link com.taobao.openimui.tribe.InviteTribeMemberActivity}
 *      邀请好友加入群
 *  {@link com.taobao.openimui.tribe.SearchTribeActivity}
 *      搜索群页面
 *  {@link com.taobao.openimui.tribe.TribeInfoActivity}
 *      群资料页面
 *  {@link com.taobao.openimui.tribe.TribeMembersActivity}
 *      群成员列表页面
 *  {@link com.taobao.openimui.tribe.TribeSystemMessageActivity}
 *      群系统消息页面，该页面用于展示群邀请消息
 *
 * com.taobao.openimui.imcore   该目录是云旺接口调用示例代码，具体如下：
 *  {@link com.taobao.openimui.imcore.ConversationSampleHelper}
 *      会话相关接口使用示例代码，该文件包含以下功能：
 *          获取会话管理器
 *          获取所有会话
 *          获取指定会话
 *          获取指定会话的最近一条消息
 *          获取所有会话的总未读数
 *          获取指定会话的未读数
 *          从会话对象中获取当前聊天的用户id
 *          从会话队形中获取群id
 *          创建跨appKey的会话
 *          创建客服会话
 *          添加会话列表更新监听
 *  {@link com.taobao.openimui.imcore.TribeSampleHelper}
 *      群功能接口使用示例代码，该文件包含以下功能：
 *          获取群管理器
 *          创建群
 *          从服务器获取当前用户所在的所有群
 *          从服务器获取单个群信息
 *          获取指定群的成员列表
 *          退出指定的群
 * 
 * @author zhaoxu
 * 
 */
public class TribeSampleHelper {

	/**
	 * 请求回调
	 * 
	 * @author zhaoxu
	 * 
	 */
	private static abstract class MyCallback implements IWxCallback {

		@Override
		public void onError(int arg0, String arg1) {
            YWLog.e("TribeSampleHelper", "code=" + arg0 + " errInfo=" + arg1);
        }

		@Override
		public void onProgress(int arg0) {

        }
	}

	public static IYWTribeService getTribeService() {
		final YWIMKit imKit = Constant.mIMKit;
		if (imKit != null) {
			return imKit.getTribeService();
		}
		return null;
	}

	/**
	 *
	 * @param tribeType，具体见YWTribeType
	 *
	 */
	public static void createTribe_Sample(YWTribeType tribeType,String name){
		final IYWTribeService tribeService = getTribeService();
		if (tribeService == null) {
			return;
		}

		YWTribeCreationParam tribeCreationParam = new YWTribeCreationParam();
		tribeCreationParam.setTribeName(name);//群名
		tribeCreationParam.setNotice("notice");//群公告
		tribeCreationParam.setTribeType(tribeType);

		if (tribeType == YWTribeType.CHATTING_GROUP){
			//讨论组需要指定用户
			final List<String> userList = new ArrayList<String>();
			final YWIMCore core = Constant.mIMKit.getIMCore();
			userList.add(core.getLoginUserId());// 当前登录的用户ID，这个必须要传
			userList.add("user2");
			tribeCreationParam.setUsers(userList);
		}

		tribeService.createTribe(new MyCallback() {
			@Override
			public void onSuccess(Object... result) {
				// 返回值为刚刚成功创建的群
				YWTribe tribe = (YWTribe) result[0];
				tribe.getTribeId();// 群ID，用于唯一标识一个群
			}
		}, tribeCreationParam);
	}

	/**
	 * 从服务器获取当前用户所在的所有群
	 */
	public static void getAllTribeFromServer_Sample() {
		final IYWTribeService tribeService = getTribeService();
		if (tribeService == null) {
			return;
		}
		tribeService.getAllTribesFromServer(new MyCallback() {

			@Override
			public void onSuccess(Object... arg0) {
				// 返回值为列表
				@SuppressWarnings("unchecked")
				List<YWTribe> tribeList = (List<YWTribe>) arg0[0];
				tribeList.size();
			}
		});
	}

	/**
	 * 从服务器获取单个群信息
	 * 
	 * @param tid
	 *            群ID
	 */
	public static void getTribeFromServer_Sample(long tid) {
		final IYWTribeService tribeService = getTribeService();
		if (tribeService == null) {
			return;
		}
		tribeService.getTribeFromServer(new MyCallback() {

			@Override
			public void onSuccess(Object... arg0) {
                YWTribe tribe = (YWTribe) arg0[0];
                tribe.getTribeId();
            }
		}, tid);
	}

	/**
	 * 获取指定群的成员列表
	 * 
	 *   回调接口, 其中 返回值为List<YWTribeMember>
	 * @param tid
	 *            群id
	 */
	public static void getMembersFromServer_Sample(long tid) {
		final IYWTribeService tribeService = getTribeService();
		if (tribeService == null) {
			return;
		}
		tribeService.getMembersFromServer(new MyCallback() {

			@Override
			public void onSuccess(Object... arg0) {
                @SuppressWarnings("unchecked")
                List<YWTribeMember> memberList = (List<YWTribeMember>) arg0[0];
                memberList.size();
            }
		}, tid);
	}

	/**
	 * 退出指定的群
	 * 
	 * @param tid
	 */
	public static void exitFromTribe_Sample(long tid) {
		final IYWTribeService tribeService = getTribeService();
		if (tribeService == null) {
			return;
		}
		tribeService.exitFromTribe(new MyCallback() {

			@Override
			public void onSuccess(Object... arg0) {
                // 成功就onSuccess，否则会调用onError
            }
		}, tid);
	}
}
