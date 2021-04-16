package com.coocaa.swaiotos.virtualinput.statemachine;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.smartscreen.utils.AndroidUtil;
import com.coocaa.swaiotos.virtualinput.utils.EmptyUtils;
import com.coocaa.swaiotos.virtualinput.utils.SpUtil;
import com.coocaa.tvpi.module.io.HomeIOThread;

import java.util.List;

/**
 * Describe:场景控制器配置类
 * Created by AwenZeng on 2020/12/16
 */
public class SceneControllerConfig {

    private static SceneControllerConfig instance;

    private List<SceneConfigBean> mSceneConfigList;

    private Context mContext;

    public static SceneControllerConfig getInstance() {
        if (instance == null) {
            synchronized (SceneControllerConfig.class) {
                if (instance == null) {
                    instance = new SceneControllerConfig();
                }
            }
        }
        return instance;
    }

    /**
     * 配置器初始化
     */
    public void init(final Context context) {
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                mContext = context.getApplicationContext();
                mSceneConfigList = HomeHttpMethod.getInstance().getSceneControlConfig();
//                SceneConfigBean extra = JSON.parseObject(extra(), SceneConfigBean.class);
                if (EmptyUtils.isEmpty(mSceneConfigList)) {
                    getSceneConfigList();
                } else {
                    SpUtil.putString(context.getApplicationContext(), SpUtil.Keys.SCENE_CONTROLLER_CONFIG, JSONObject.toJSONString(mSceneConfigList));
                }
//                mSceneConfigList.add(extra);
            }
        });
    }


    /**
     * 获取场景遥控器配置列表<br>
     * 描述：<br>
     * 1.每次从网络中拉去，会缓存一份到手机<br>
     * 2.当获取配置列表时，如果配置列表为空，优先从缓存中获取配置数据<br>
     * 3.缓存中没有，再从本地配置json中获取<br>
     *
     * @return
     */
    public List<SceneConfigBean> getSceneConfigList() {
        if (EmptyUtils.isEmpty(mSceneConfigList)) {
            String cacheConfig = null;
            if (EmptyUtils.isNotEmpty(mContext)) {
                cacheConfig = SpUtil.getString(mContext, SpUtil.Keys.SCENE_CONTROLLER_CONFIG);
            }
            if (EmptyUtils.isNotEmpty(cacheConfig)) {
                mSceneConfigList = JSONArray.parseArray(cacheConfig, SceneConfigBean.class);
            } else {
                mSceneConfigList = JSONArray.parseArray(AndroidUtil.readAssetFile("scene_control_config.json"), SceneConfigBean.class);
            }
        }
        return mSceneConfigList;
    }

    /**
     * 获取场景配置信息
     *
     * @param id
     * @return
     */
    public SceneConfigBean getSceneControllerInfo(String id) {
        if (EmptyUtils.isNotEmpty(id)) {
            for (SceneConfigBean item : mSceneConfigList) {
                if (item.id.equals(id)) {
                    return item;
                }
            }
        }
        return getDefaultControllerInfo();
    }

    /**
     * 获取默认的数据
     *
     * @return
     */
    public SceneConfigBean getDefaultControllerInfo() {
        SceneConfigBean sceneConfigBean = new SceneConfigBean();
        sceneConfigBean.id = "DEFAULT";
        sceneConfigBean.titleFormat = "共享屏暂未投送内容";
        sceneConfigBean.subTitle = "投送内容后，点击这里可以遥控";
        sceneConfigBean.appletName = "";
        sceneConfigBean.appletIcon = "";
        sceneConfigBean.appletUri = "";
        sceneConfigBean.contentType = "np";
        sceneConfigBean.contentUrl = "DEFAULT";
        sceneConfigBean.scale = 1;
        return sceneConfigBean;
    }

//    private String extra() {
//        return "{\n" +
//                "    \"id\":\"swaiotos.runtime.h5.app$browser\",\n" +
//                "    \"scale\":\"1\",\n" +
//                "    \"subTitle\":\"\",\n" +
//                "    \"appletUri\":\"np://com.coocaa.smart.browser/index\",\n" +
//                "    \"appletIcon\":\"https://tv.doubimeizhi.com/images/dongle/app/v2/contents/link.png\",\n" +
//                "    \"appletName\":\"链接\",\n" +
//                "    \"contentUrl\":\"BROWSER\",\n" +
//                "    \"contentType\":\"np\",\n" +
//                "    \"titleFormat\":\"%u共享了链接\"\n" +
//                "}";
//    }
}
