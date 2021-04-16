## 新TV智家插件接口对接文档
本工程是宿主（智家）与插件的公共接口。插件实现这些接口，宿主加载插件对这些接口的实现类，并实例化。插件代码运行于宿主进程中。

### 1、插件形式
插件打包成apk，插件可以单独启动，显示自己的UI页面，也可以作为插件形式，将某个UI显示在宿主中。

### 2、插件工程如何配置
1、插件工程依赖本模块，在build.gradle中的dependencies必须`compileOnly(gradle >= 3.0)`或`provided(gradle < 3.0)`本模块。

2、由插件实现模块的接口类`ISmartHomePluginInterface`。

3、在插件App的AndroidManifest.xml中，配置meta-data，用于标记插件实现类，宿主根据此标记来加载类。  
其中，`android:name`的值必须是`SMART_HOME_PLUGIN`  
`android:value`的值就是插件实现`ISmartHomePluginInterface`接口的具体类的包名类名。

```xml
<manifest>
<application>
	<meta-data
	android:name="SMART_HOME_PLUGIN"
	android:value="xxx.xxx.xxx"/>
</application>
</manifest>
```

### 3、插件工程配置
1、电脑需要添加`ANDROID_HOME`的环境变量配置，正常安装Android SDK后都会有此配置，可以自行检查，Gradle Sync 或者Run事后，插件接口工程也会打印该配置，如果是null，表示没读到或者没配置对，会找不到Android相关方法，就会报错。

2、插件作为独立的APP工程，需要在build.gradle中添加对本接口工程的依赖

### 4、混淆
如果插件工程需要混淆，那么需要在插件工程的proguard-rules.pro中添加如下keep保护：

```
-keep class * implements com.skyworth.smarthome_tv.smarthomeplugininterface.ISmartHomePluginInterface {*;}
-keep class * implements java.io.Serializable {*;}
-keep class android.content.res.**
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }
```

### 5、接口API
以下方法由插件实现，被宿主调用。

```java
/**
 * 插件接收宿主传来的Context。
 * @param pluginContext 插件Context
 */
void onContextSet(Context pluginContext);
```

```java
/**
 * 插件加载完成后，第一次调用。
 */
void onPluginInit();
```

```java
/**
 * 插件提供主页卡片View。
 * @param callback 插件布局内部焦点边界回调
 */
View getContentCardView(IViewBoundaryCallback callback);
```

```java
/**
 * 插件提供智慧家庭App列表PanelView。
 * @param callback 插件布局内部焦点边界回调
 */
View getPanelView(IViewBoundaryCallback callback);
```

```java
/**
 * 首屏卡片View生命周期回调，由插件实现后提供给宿主调用。
 */
LifeCycleCallback getContentLifeCycleCallback();
```

```java
/**
 * 智慧家庭App内部列表panleView生命周期回调，由插件实现后提供给宿主调用。
 */
LifeCycleCallback getPanelLifeCycleCallback();
```

### 6、注意事项
1、插件作为独立APP时，会在自己的Application中做一些初始化动作，当插件在主页框架中加载的时候，是不会走到Application初始化的，所以需要插件在自己的接口实现类中，补充做一些必要的初始化动作，必要是指只有插件UI页面需要的动作，不是插件UI页面需要的都不需要处理，因为点击跳转页面后就会拉起插件APP的进程，会走到插件Application的初始化。

2、如果插件代码中，是显示启动的内部Activity，示例代码如下：

```java
Intent intent = new Intent();
intent.setClass(pluginContext, MActivity.class);
pluginContext.startActivity(intent);
```

插件UI页面被start的Activity需要在AndroidManifest.xml中注册的时候，添加android:exported="true"，如下：

```xml
<activity
   android:name=".MyActivity"
   android:exported="true">
</activity>
```

如果是通过action/category/uri等隐式方式启动的activity，则activity不需要添加exported true

```java
Intent intent = new Intent("com.xxx.action.yy.xx");
pluginContext.startActivity(intent);
```