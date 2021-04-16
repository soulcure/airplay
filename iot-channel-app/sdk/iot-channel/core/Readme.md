# iot-channel sdk

* ## 创建业务侧处理服务
每个apk可以有一个或多个此服务，按业务结构来设计即可。 
每个服务的注册步骤 

* 1、新建Service  
```
class MyClientService extends SSClientService
```

* 2、Manifest中对应的Service增加intent-filter 

```
<intent-filter>
    <action android:name="swaiotos.intent.action.channel.iot.SSCLIENT" />
    <category android:name="swaiotos.intent.category.channel.iot.SSCLIENT" />
</intent-filter>
```

* 3、Manifest中对应的Service增加auth和key的metadata
```
<meta-data
    android:name="auth"
    android:value="swaiotos.channel.iot.tv.demo1" />
<meta-data
    android:name="key"
    android:value="key~1234567" />
```

* example:
```
<service
    android:name=".demo1.SSClient1Service"
    android:enabled="true"
    android:exported="true"
    android:process=":demo1">
<intent-filter>
    <action android:name="swaiotos.intent.action.channel.iot.SSCLIENT" />
    <category android:name="swaiotos.intent.category.channel.iot.SSCLIENT" />
</intent-filter>

<meta-data
    android:name="auth"
    android:value="swaiotos.channel.iot.tv.demo1" />
</service>
```
