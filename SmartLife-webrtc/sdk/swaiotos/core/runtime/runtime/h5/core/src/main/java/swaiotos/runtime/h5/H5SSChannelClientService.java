package swaiotos.runtime.h5;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.businessstate.object.BusinessState;
import com.coocaa.businessstate.object.User;
import com.coocaa.smartsdk.SmartApi;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.runtime.Applet;
import swaiotos.runtime.h5.common.event.WebControlEvent;
import swaiotos.runtime.h5.core.os.H5RunType;

public class H5SSChannelClientService extends SSChannelClient.SSChannelClientService {
    public static final String TAG = "H5SSCCS";

    public H5SSChannelClientService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i(TAG, "H5SSChannelClientService: onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "H5SSChannelClientService: onCreate");
    }

    @Override
    protected boolean handleIMMessage(IMMessage imMessage, SSChannel ssChannel) {
        Log.i(TAG, "handleIMMessage  type:" + imMessage.getType());
        Log.i(TAG, "handleIMMessage  id: " + imMessage.getId());
        Log.i(TAG, "handleIMMessage  content:" + imMessage.getContent());
        Log.i(TAG, "handleIMMessage  source:" + imMessage.getSource());
        Log.i(TAG, "handleIMMessage  target:" + imMessage.getTarget());
        Log.i(TAG, "handleIMMessage  clientSource:" + imMessage.getClientSource());
        Log.i(TAG, "handleIMMessage  clientTarget:" + imMessage.getClientTarget());
        Log.i(TAG, "handleIMMessage  extra:" + imMessage.encode());

        String owner = imMessage.getExtra("owner");
        Log.i(TAG, "handleIMMessage  owner:" + owner);
        if(owner != null) {
            try {
                H5CacheState.owner = User.decode(owner);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 如果是打开浏览器的命令，则先执行打开浏览器
        String content = imMessage.getContent();
        if (content.length() > 0 && content.charAt(0) == '{') {
            JSONObject obj = null;
            try {
                obj = JSONObject.parseObject(content);
            } catch (Exception e) {
                obj = null;
            }
            if (obj != null && obj.containsKey("do")) {
                String action = (String) obj.get("do");
                if (action.equalsIgnoreCase("launcher_browser")) {
                    String url = (String) obj.get("url");
                    String name = (String) obj.get("name");
                    String flag = (String) obj.get("flag");
                    int colonIndex = url.indexOf(":");                      // 在url中找到冒号分隔符的位置
                    String protocol = url.substring(0, colonIndex);         // http or https
                    String residual = url.substring(colonIndex + 3);        // 剩下域名和路径 domain-name + path
                    int rootPathIndex = residual.indexOf("/");              // 剩下部分中找到第一个 "/"
                    String domain;
                    String path;
                    if (rootPathIndex > 0) {
                        domain = residual.substring(0, rootPathIndex);
                        path = residual.substring(rootPathIndex);
                    } else{
                        domain = residual;
                        if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https"))
                            path = "/";
                        else
                            path = "";
                    }
                    String pageType = (String) obj.get("pageType");
                    startWebPage(protocol, domain, path, name, flag, owner, pageType);
                    return true;
                } else if (action.equalsIgnoreCase("web_control")) {
                    WebControlEvent event = new WebControlEvent();
                    event.cmd = obj.getString("cmd");
                    event.extra = obj.getString("extra");
                    event.message = imMessage;
                    EventBus.getDefault().post(event);
                }
            }
        }

        EventBus.getDefault().post(imMessage);
        Log.i(TAG, "handleIMMessage: finish: ");
        return true;
    }

    private boolean startWebPage(String protocol, String domain, String path, String name, String flag, String owner, String pageType) {
        Log.i(TAG, "H5SSChannelClientService: startWebPage, protocol=" + protocol + ", domain=" + domain + ", path=" + path + ", name=" + name + ", flag=" + flag
             + ", owner=" + owner + ", pageType=" + pageType);
        HashMap<String, String> runtimeMap = new HashMap<>();
        runtimeMap.put(H5RunType.RUNTIME_KEY, H5RunType.TV_RUNTYPE);
        runtimeMap.put(H5RunType.RUNTIME_NAV_KEY, H5RunType.RUNTIME_NAV_TOP);
        runtimeMap.put(H5RunType.RUNTIME_TRANSITION_KEY, H5RunType.RUNTIME_TRANSITION_FROM_BOTTOM);
        try {
            Log.i(TAG, "H5SSChannelClientService isMobile=" + SmartApi.isMobileRuntime());
            if(SmartApi.isMobileRuntime()) {
                Applet applet = new Applet(protocol, domain, path, name, "", 100001, runtimeMap, null);
                H5AppletRunner.get().start(getApplicationContext(), applet);
            } else {
                Intent intent = new Intent();
                intent.setClassName(getPackageName(),"swaiotos.runtime.h5.app.H5TVAppletActivity");
                intent.putExtra("url", buildUrl(protocol, domain, path));
                intent.putExtra(H5RunType.RUNTIME_KEY, H5RunType.TV_RUNTYPE);
                if(!TextUtils.isEmpty(owner)) {
                    User mUser = User.decode(owner);
                    String gameType = this.getPackageName() + "$";
                    if(TextUtils.isEmpty(pageType)) {
                        gameType = gameType + "H5_PAGE_GAME";
                    } else {
                        gameType = gameType + pageType;
                    }
                    BusinessState state = new BusinessState.Builder().owner(mUser).id(gameType).build();
                    intent.putExtra("r_state", BusinessState.encode(state));
                }

                if (!(getBaseContext() instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startActivity(intent);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String buildUrl(String protocol, String domain, String path) {
        Log.d(TAG, "buildUrl() called with: protocol = [" + protocol + "], domain = [" + domain + "], path = [" + path + "]");
        String url = new Uri.Builder().scheme(protocol).authority(domain).build().toString() + path;
        Log.d(TAG, "buildUrl() return url:  " + url);
        return url;
    }
}

