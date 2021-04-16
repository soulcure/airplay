# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.

-optimizationpasses 5
#混淆时不使用大小写混合，混淆后的类名为小写
-dontusemixedcaseclassnames
#指定不去忽略非公共的库的类
-dontskipnonpubliclibraryclasses
#指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers
#不做预校验，preverify是proguard的4个步骤之一
#Android不需要preverify，去掉这一步可加快混淆速度
-dontpreverify
#有了verbose这句话，混淆后就会生成映射文件#包含有类名->混淆后类名的映射关系
#然后使用printmapping指定映射文件的名称
-verbose-printmapping proguardMapping.txt
#指定混淆时采用的算法，后面的参数是一个过滤器
#这个过滤器是谷歌推荐的算法，一般不改变
-optimizations !code/simplification/cast,!field/*,!class/merging/*
#保护代码中的Annotation不被混淆，这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*,InnerClasses
#避免混淆泛型，这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature
#抛出异常时保留代码行号，在异常分析中可以方便定位
-keepattributes SourceFile,LineNumberTable


-keepclassmembers class * implements java.io.Serializable {
   static final long serialVersionUID;
   private static final java.io.ObjectStreamField[] serialPersistentFields;
   private void writeObject(java.io.ObjectOutputStream);
   private void readObject(java.io.ObjectInputStream);
   java.lang.Object writeReplace();
   java.lang.Object readResolve();
}

#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable { *; }
#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable { *; }
-keep class * implements java.io.Serializable {*;}
-keep class * implements android.os.Parcelable {*;}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#javabean不混淆
-keep class com.coocaa.tvpi.data.** { *; }

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#netease start
-dontwarn com.netease.**
-keep class com.netease.** {*;}
#如果你使用全文检索插件，需要加入
-dontwarn org.apache.lucene.**
-keep class org.apache.lucene.** {*;}
#netease end

#友盟
 -dontwarn com.umeng.**
 -keep class com.umeng.**{*;}
 -keep class org.android.**{*;}
 -keep class com.squareup.wire.**{*;}
 -keep class com.ta.utdid2.**{*;}
 -keep class com.ut.device.**{*;}
 -keep,allowshrinking class org.android.agoo.service.* {
    public <fields>;
    public <methods>;
}
-keep,allowshrinking class com.umeng.message.* {
    public <fields>;
    public <methods>;
}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#友盟 end

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

## ---------Retrofit混淆方法---------------
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class com.squareup.okhttp3.** {*;}
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
-dontwarn java.util.concurrent.Flow*

# Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod

#EventBus
-keep class de.greenrobot.event.** {*;}
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}

#闪验
-dontwarn com.baidu.**
-dontwarn com.tencent.**
-dontwarn com.cmic.sso.sdk.**
-keep class com.cmic.sso.sdk.**{*;}
-dontwarn com.sdk.**
-keep class com.sdk.** { *;}
-dontwarn com.unicom.xiaowo.account.shield.**
-keep class com.unicom.xiaowo.account.shield.** {*;}
-keep class cn.com.chinatelecom.account.api.**{*;}

##weex_base
-keep class app.eeui.framework.**{*;}
-keep class eeui.android.eeuiPictrue.**{*;}
-keep class com.alibaba.weex.pluginmanager.**{*;}
-keep class com.skyworth.weexbase.**{*;}
-keep class com.tianci.skylink.protocol.**{*;}

-keep class org.apache.weex.bridge.** { *; }
-keep class org.apache.weex.layout.** { *; }
-keep class org.apache.weex.WXSDKEngine { *; }
-keep class org.apache.weex.base.SystemMessageHandler { *; }
-dontwarn org.apache.weex.bridge.**

-keep class com.taobao.weex.WXDebugTool{*;}
-keep class com.taobao.weex.devtools.common.LogUtil{*;}
-keepclassmembers class ** {
  @com.taobao.weex.ui.component.WXComponentProp public *;
}
-keep class com.taobao.weex.bridge.**{*;}
-keep class com.taobao.weex.dom.**{*;}
-keep class com.taobao.weex.adapter.**{*;}
-keep class com.taobao.weex.common.**{*;}
-keep class * implements com.taobao.weex.IWXObject{*;}
-keep class com.taobao.weex.ui.**{*;}
-keep class com.taobao.weex.ui.component.**{*;}
-keep class com.taobao.weex.utils.**{
    public <fields>;
    public <methods>;
    }
-keep class com.taobao.weex.view.**{*;}
-keep class com.taobao.weex.module.**{*;}
-keep public class * extends com.taobao.weex.common.WXModule{*;}
-keep public class * extends com.taobao.weex.ui.component.WXComponent{*;}
-keep class * implements com.taobao.weex.ui.IExternalComponentGetter{*;}
-keep class com.skyworth.weexbase.components.**{*;}
-keep class com.skyworth.weexbase.module.**{*;}

-keep class com.net.**{*;}
-keep class com.skyworth.aiot.client.**{*;}

#iot
-keep class com.skyworthiot.iotssemsg.**{*;}


-keep class com.baidubce.**{*;}
-dontwarn com.baidubce.**

-keep class swaiotos.channel.iot.**{*;}
-dontwarn swaiotos.channel.iot.**


#华为
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
#huawei scan
-keep class com.huawei.hianalytics.**{*;}

#tbs
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
-keep class com.tencent.smtt.**{*;}
-keep class com.tencent.tbs.**{*;}
-keep class com.tencent.** {*;}

## android
-dontwarn android.content.pm.**
-keep class android.content.pm.** {*;}
-dontwarn android.content.res.**
-keep class android.content.res.** {*;}

-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }


# eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
# end eventbus

#coocaa
-keep class swaiotos.sal.** { *; }
-keepattributes SourceFile,LineNumberTable

#smartscreen data
-dontwarn com.coocaa.smartscreen.data.**
-keep class com.coocaa.smartscreen.data.** { *; }
-dontwarn com.coocaa.publib.data.**
-keep class com.coocaa.publib.data.** { *; }
-dontwarn swaiotos.runtime.h5.**
-keep class swaiotos.runtime.h5.** { *; }


#-keep class swaiotos.runtime.h5.** { *; }

-keepclassmembers class ** {
  @android.webkit.JavascriptInterface public *;
}


##网易七鱼
-dontwarn com.qiyukf.**
-keep class com.qiyukf.** {*;}
-dontwarn com.netease.**
-keep class com.netease.** {*;}
-dontwarn org.slf4j.** {*;}
-keep class org.slf4j.** { *; }




