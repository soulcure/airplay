# iot-channel sdk

* ## 创建业务侧ClientService/ClientActivity
每个apk可以有一个或多个ClientService/ClientActivity，按业务结构来设计即可。 
每个ClientService/ClientActivity的注册步骤 

* 1、新建Service  
```
class MyClientService extends SSChannelClient.SSChannelClientService
```

* 2、新建Activity
```
class MyClientActivity extends SSChannelClient.SSChannelClientActivity
```
如果不方便继承自SSChannelClient.SSChannelClientActivity，可以使用
```
SSChannelClient.parseMessage(Intent intent)

SSChannelClient.parseChannel(Intent intent)
```
来获取收到的IMMessage和SSChannel实例

* example:
```
    ......

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();

        IMMessage message = parseMessage(intent);
        if (mSSChannel == null) {
            mSSChannel = parseChannel(intent);
        }
        if (mSSChannel != null) {
            handleIMMessage(message, mSSChannel);
        }
    }

    ......
```

* 3、Manifest中对应的Service/Activity增加intent-filter

这里的IntentFilter由平台SSChannelService决定
```
<intent-filter>
    <action android:name="swaiotos.intent.action.channel.iot.SSCLIENT" />
</intent-filter>
```

* 4、Manifest中对应的Service/Activity增加clientID和clientKey的metadata
```
<meta-data
    android:name="ss-clientID"
    android:value="ss-clientID-mobile" />
<meta-data
    android:name="ss-clientKey"
    android:value="ss-clientKey-12345" />
```

* example:
```
<service
    android:name=".MainSSClientService"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="swaiotos.intent.action.channel.iot.SSCLIENT" />
    </intent-filter>

    <meta-data
    android:name="ss-clientID"
    android:value="swaiotos.channel.iot.tv.demo1" />
    <meta-data
    android:name="ss-clientKey"
    android:value="key~1234567" />
    <meta-data
    android:name="ss-clientVersion"
    android:value="1" />
</service>

<activity
    android:name=".demo1.Demo1ReadActivity"
    android:process=":demo1">

    <intent-filter>
        <action android:name="swaiotos.intent.action.channel.iot.SSCLIENT" />
    </intent-filter>

    <meta-data
    android:name="ss-clientID"
    android:value="swaiotos.channel.iot.tv.demo1" />
    <meta-data
    android:name="ss-clientKey"
    android:value="key~1234567" />
    <meta-data
    android:name="ss-clientVersion"
    android:value="1" />
</activity>

```

* ## 使用SSChannel
    SSChannel封装了IOT-Channel提供的所有能力，包括连接/断开设备管理，收发IM消息，收发Stream

* 1、获取SSChannel实例

    有两种方式获取SSChannel实例
    
    a、实现的Client端可以从Intent中获取实例
    ```
    SSChannelClient.parseChannel(Intent intent)
    ```

    b、主动获取实例

    IOTChannel.manager提供了若干open的方式，按需使用
    ```
    /**
     * 同步绑定，此方法绑定的SSChannelService是context所在package中的实现
     *
     * @param context the context
     * @return the ss channel
     */
    SSChannel open(Context context) throws Exception;

    /**
     * 同步绑定，此方法绑定的SSChannelService是packageName所指定的package中的实现
     *
     * @param context     the context
     * @param packageName the package name
     * @return the ss channel
     */
    SSChannel open(Context context, String packageName) throws Exception;

    /**
     * 异步绑定，此方法绑定的SSChannelService是context所在package中的实现
     *
     * @param context  the context
     * @param callback the callback
     */
    void open(Context context, OpenCallback callback);

    /**
     * 异步绑定，此方法绑定的SSChannelService是packageName所指定的package中的实现
     *
     * @param context     the context
     * @param packageName the package name
     * @param callback    the callback
     */
    void open(Context context, String packageName, OpenCallback callback);
    ```

* 1、设备管理

    获取到SSChannel实例后，通过Controller提供的接口来完成设备的连接和断开操作SS