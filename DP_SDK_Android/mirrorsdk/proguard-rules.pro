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

-keep public class * extends android.app.Service


-keep public class com.swaiotos.skymirror.sdk.data.DeviceInfo { *; }
-keep public class com.swaiotos.skymirror.sdk.data.TouchData { *; }
-keep public class com.swaiotos.skymirror.sdk.capture.MirManager { *; }
-keep public class com.swaiotos.skymirror.sdk.capture.IAudioService { *; }
-keep public class com.swaiotos.skymirror.sdk.capture.IVideoService { *; }
-keep public class com.swaiotos.skymirror.sdk.capture.INfcServiceCallback { *; }
-keep public class com.swaiotos.skymirror.sdk.capture.IDeviceInfoCallBack { *; }
-keep public class com.swaiotos.skymirror.sdk.capture.ConnectResultListener { *; }
-keep public class com.swaiotos.skymirror.sdk.capture.IVideoServiceCallback { *; }
-keep public class com.swaiotos.skymirror.sdk.reverse.IPlayerListener { *; }
-keep public class com.swaiotos.skymirror.sdk.reverse.IPlayerInitListener { *; }
-keep public class com.swaiotos.skymirror.sdk.capture.MirClientService {
    protected abstract void initNotification();
}
-keep public class com.swaiotos.skymirror.sdk.reverse.ReverseCaptureService {
    protected abstract void initNotification();
}
