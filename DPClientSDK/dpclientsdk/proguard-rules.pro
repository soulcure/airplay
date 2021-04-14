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

# 关闭压缩
-dontshrink
#代码混淆压缩比，在0和7之间，默认为5，一般不需要改
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
#-verbose-printmapping proguardMapping.txt
#指定混淆时采用的算法，后面的参数是一个过滤器
#这个过滤器是谷歌推荐的算法，一般不改变
-optimizations !code/simplification/cast,!field/*,!class/merging/*
#保护代码中的Annotation不被混淆，这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*,InnerClasses
#避免混淆泛型，这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature
#抛出异常时保留代码行号，在异常分析中可以方便定位
-keepattributes SourceFile,LineNumberTable

-keep public enum com.skyworth.dpclientsdk.ConnectState{*;}

-keep public interface com.skyworth.dpclientsdk.RequestCallback{*;}

-keep public interface com.skyworth.dpclientsdk.ResponseCallback{*;}

-keep public interface com.skyworth.dpclientsdk.StreamSinkCallback{*;}

-keep public interface com.skyworth.dpclientsdk.StreamSourceCallback{*;}

-keep public class com.skyworth.dpclientsdk.WebSocketServer {*;}

-keep public class com.skyworth.dpclientsdk.WebSocketClient {*;}

-keep public class com.skyworth.dpclientsdk.TcpClient {*;}

-keep public class com.skyworth.dpclientsdk.TcpServer {*;}
