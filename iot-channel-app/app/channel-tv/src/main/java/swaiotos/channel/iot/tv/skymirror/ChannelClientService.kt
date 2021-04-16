package swaiotos.channel.iot.tv.skymirror;

import android.content.Intent
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.swaiotos.skymirror.sdk.capture.MirManager
import com.swaiotos.skymirror.sdk.capture.MirManager.InitListener
import com.swaiotos.skymirror.sdk.reverse.PlayerActivity
import swaiotos.channel.iot.common.utils.BindCodeUtil
import swaiotos.channel.iot.common.utils.BindCodeUtil.BindCodeCall
import swaiotos.channel.iot.common.utils.TYPE
import swaiotos.channel.iot.ss.SSChannel
import swaiotos.channel.iot.ss.SSChannelClient
import swaiotos.channel.iot.ss.channel.im.IMMessage
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback
import swaiotos.channel.iot.utils.DeviceUtil

/**
 * @ClassName: ChannelClientService
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/5/11 11:17
 */
class ChannelClientService : SSChannelClient.SSChannelClientService("ChannelClientService") {

    private val TAG = ChannelClientService::class.java.simpleName

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun handleIMMessage(message: IMMessage?, channel: SSChannel?): Boolean {

        Log.e("cmd", "handleIMMessage: message --- $message")
        val content = message!!.content
        Log.e("cmd", "handleIMMessage: content --- $content")
        try {
            val gson = Gson()
            val msg = gson.fromJson(content, CmdData::class.java)
            //接收到接收端开始消息,正常状况，开启MirClientService，开始录屏
            if (msg.cmd == ReverseScreenParams.CMD.START_REVERSE.toString()) {
                if (TextUtils.isEmpty(msg.param)) {
                    Log.e("MirClientService", "iot-channel param is null")
                    return false
                }

                if (MirManager.instance().isMirRunning) {//已经被同屏控制
                    //正在镜像，通知对方退出
                    val reverse = ReverseScreenParams()
                    reverse.ip = "";
                    val cmdData = CmdData(
                            ReverseScreenParams.CMD.STOP_REVERSE.toString(),
                            CmdData.CMD_TYPE.REVERSE_SCREEN.toString(),
                            reverse.toJson()
                    )

                    if (channel != null) {
                        sendChannelMessage(message, channel, cmdData.toJson())
                    }
                    Log.e("MirClientService", "自己已经被同屏控制了，通知对方退出...")

                } else { //没有被同屏控制
                    val serverIp = gson.fromJson(msg.param, ServerIpData::class.java).ip
                    if (TextUtils.isEmpty(serverIp)) {
                        Log.e("MirClientService", "iot-channel ip is null")
                        return false
                    }
                    //开始镜像
                    Log.d("MirClientService", "START_REVERSE from iot-channel ip---$serverIp")

                    MirManager.instance().startScreenCapture(this, serverIp)
                    Log.e("colin", "colin start time01 --- tv start MirClientService by iot-channel")
                }

            }

            //接收到接收端结束消息
            else if (msg.cmd == ReverseScreenParams.CMD.STOP_REVERSE.toString()) {
                //结束所有服务
                MirManager.instance().stopAll(this);
                Toast.makeText(this, "同屏控制失败", Toast.LENGTH_SHORT).show()

                Log.e("MirClientService", "目标已经被同屏控制了...")
                return true;
            }
            //电视端接收到 屏幕镜像 消息，正常状况，绑定ReverseCaptureService，启动PlayerActivity，开始解码播放
            else if (msg.cmd == MirrorScreenParams.CMD.START_MIRROR.toString()) {
                if (MirManager.instance().isReverseRunning) {
                    //正在镜像，通知对方退出
                    val mirror = MirrorScreenParams()
                    mirror.result = false;

                    val cmdData = CmdData(
                            MirrorScreenParams.CMD.STOP_MIRROR.toString(),
                            CmdData.CMD_TYPE.MIRROR_SCREEN.toString(),
                            mirror.toJson()
                    )
                    if (channel != null) {
                        sendChannelMessage(message, channel, cmdData.toJson())
                    }

                    Log.e("PlayerDecoder", "目标已经被屏幕镜像了，通知对方退出...")

                } else {
                    MirManager.instance().init(this,
                            object : InitListener {
                                override fun success() {
                                    PlayerActivity.obtainPlayer(this@ChannelClientService) { b ->
                                        if (b && channel != null) {

                                            val mirror = MirrorScreenParams()
                                            mirror.result = true;
                                            mirror.ip = DeviceUtil.getLocalIPAddress(this@ChannelClientService)
                                            val cmdData = CmdData(
                                                    MirrorScreenParams.CMD.START_MIRROR.toString(),
                                                    CmdData.CMD_TYPE.MIRROR_SCREEN.toString(),
                                                    mirror.toJson()
                                            )

                                            sendChannelMessage(message, channel, cmdData.toJson())
                                            Log.d("PlayerDecoder", "屏幕镜像TV端初始化完成，发送指令给PAD开始传屏")
                                        }
                                    }
                                }

                                override fun fail() {
                                    Toast.makeText(
                                            this@ChannelClientService,
                                            "解码服务绑定失败", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })

                    Log.d("PlayerDecoder", "屏幕镜像开始，TV端开始初始化...")
                }



                return true;
            }
            //接收到发送端结束消息
            else if (msg.cmd == MirrorScreenParams.CMD.STOP_MIRROR.toString()) {
                //结束所有服务
                MirManager.instance().stopAll(this);
                Toast.makeText(this, "屏幕镜像失败", Toast.LENGTH_SHORT).show()
                return true;
            }
            //信发消息开始
            else if (msg.cmd == LetterSendParams.CMD.START_LETTER.toString()) {
                Log.d(TAG, "handleIMMessage: START_LETTER")
                val reverse = LetterSendParams()
                BindCodeUtil.getInstance().startHeartBeat(this, TYPE.TV, object : BindCodeCall {
                    override fun onBindBitmapShow(bindCode: String, url: String, expiresIn: String) {
                        reverse.bindCode = bindCode
                        reverse.url = url
                        reverse.expiresIn = expiresIn
                        Log.d(TAG, "onBindBitmapShow: " + bindCode)
                        val cmdData = CmdData(
                                LetterSendParams.CMD.START_LETTER.toString(),
                                CmdData.CMD_TYPE.LETTER_SEND.toString(),
                                reverse.toJson()
                        )
                        if (channel != null) {
                            sendChannelMessage(message, channel, cmdData.toJson())
                        }
                    }
                })
            }
            //信发消息结束
            else if (msg.cmd == LetterSendParams.CMD.STOP_LETTER.toString()) {
                Log.d(TAG, "handleIMMessage: STOP_LETTER")
                BindCodeUtil.getInstance().stopHeartBeat();
                return true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }


    /**
     * 回复对方消息
     */
    private fun sendChannelMessage(message: IMMessage, channel: SSChannel, cmd: String): Unit {
        try {
            Log.d("cmd", "target --- " + message.target)
            Log.d("cmd", "source --- " + message.source)
            Log.d("cmd", "clientTarget --- " + message.clientTarget)
            Log.d("cmd", "clientSource --- " + message.clientSource)
            Log.d("cmd", "cmdData --- $cmd")

            val imMessage = IMMessage.Builder.createTextMessage(
                    /*message.source,*/  message.target,
                    /*message.target,*/ message.source,
                    /*message.clientSource,*/message.clientTarget,
                    /*message.clientTarget,*/ message.clientSource,
                    cmd
            )

            imMessage.putExtra("response", cmd)

            channel.imChannel.send(imMessage, object : IMMessageCallback {
                override fun onProgress(message: IMMessage?, progress: Int) {
                    Log.e("cmd", "onProgress --- " + progress)
                }

                override fun onEnd(message: IMMessage?, code: Int, info: String?) {
                    Log.e("cmd", "onEnd --- " + code)
                }

                override fun onStart(message: IMMessage?) {
                    Log.e("cmd", "onStart --- ")
                }
            })
        } catch (e: Exception) {
            Log.e("cmd", "iot-channel reSend failed --- " + e.message)
        }

    }
}