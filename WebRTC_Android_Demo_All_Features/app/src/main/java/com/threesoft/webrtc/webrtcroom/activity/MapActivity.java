package com.threesoft.webrtc.webrtcroom.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.threesoft.webrtc.webrtcroom.R;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.NotifyJoinRoomMsg;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.OfflineMsg;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.User;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.Users;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.WebRtcClient2;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements SensorEventListener  {

    private static final String TAG = "MapActivity";
    // 定位相关
    private LocationClient mLocClient;
    private MyLocationListener myListener = new MyLocationListener();
    // 定位图层显示方式
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private float mCurrentAccracy;
    // 初始化地图
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    // 是否首次定位
    private boolean isFirstLoc = true;
    // 是否开启定位图层
    private boolean isLocationLayerEnable = true;
    private MyLocationData myLocationData;
//    private ArrayList<User> mUsers = new ArrayList<>();
    private Users mUsers ;
    private HashMap<String, Overlay> mOverlays = new HashMap<>();
    private User mMyself = new User();
    private double mCurrentLat = 0.0;
    private double mCurrentLng = 0.0;

    //记录用户首次点击返回键的时间
    private long firstTime = 0;
    private NetWorkStateReceiver netWorkStateReceiver;

    private ImageButton switchModel;
    private ListView usersListView;
    private boolean isListModel = false;
    private UserAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActManager.addActivity(MapActivity.this);
        setContentView(R.layout.activity_map);
        Log.d(TAG,"onCreate");
        // 设置地图及定位初始化
        //检查是否已经授予权限
        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if (!Settings.canDrawOverlays(this)) {
                //若未授权则请求权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }
        }

        switchModel = findViewById(R.id.ib_switch_model);
        switchModel.setOnClickListener(switchModelListener);

        usersListView = findViewById(R.id.lv_users);
        initListView();

        initLocation();

        //注册
        EventBus.getDefault().register( this );

    }


    private  View.OnClickListener switchModelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(mUsers != null && mUsers.peers != null){
                isListModel = !isListModel;
                if(isListModel){
                    adapter.clear();
                    for(User user:mUsers.peers.values()){
                        if(!mMyself.socketId.equals(user.socketId)){
                            adapter.add(user);
                        }

                    }
                    adapter.notifyDataSetChanged();
                    usersListView.setVisibility(View.VISIBLE);
                    switchModel.setBackground(getDrawable(R.mipmap.map));
                }else{
                    usersListView.setVisibility(View.INVISIBLE);
                    switchModel.setBackground(getDrawable(R.mipmap.users));
                }
            }

        }
    };

    private void initListView(){

        adapter = new UserAdapter(MapActivity.this);
        usersListView.setAdapter(adapter);
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = adapter.getItem(i);


                if(mMyself != null){
                    if(mMyself.socketId.equals(user.socketId)){
                        Log.d(TAG,"user list click myself ,no talk...");
                        Toast.makeText(MapActivity.this,"不能和自己聊天",Toast.LENGTH_LONG).show();
                    }else {
                        //点击头像，跳转到聊天页面
                        Intent intent = new Intent();
                        intent.putExtra("from",mMyself.socketId);
                        intent.putExtra("to",user.socketId);
                        intent.putExtra("room",mMyself.socketId);
                        intent.putExtra("reason",1);
                        intent.setClass(MapActivity.this,ChatActivity2.class);
                        startActivity(intent);
                    }

                }

            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        // 在activity执行onResume时必须调用mMapView. onResume ()
        mMapView.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        // 在activity执行onPause时必须调用mMapView. onPause ()
        mMapView.onPause();
        unRegisterNetworkReceiver();
        super.onPause();

    }


    @Override
    protected void onDestroy() {

        Log.d(TAG,"onDestroy");
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // 在activity执行onDestroy时必须调用mMapView.onDestroy()
        mMapView.onDestroy();
        //取消注册 , 防止Activity内存泄漏
        EventBus.getDefault().unregister( this ) ;
        saveMyPos();
        Thread.setDefaultUncaughtExceptionHandler(null);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
       // Log.d(TAG,"onSensorChanged");
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            myLocationData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)// 设置定位数据的精度信息，单位：米
                    .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(mCurrentLat)
                    .longitude(mCurrentLng)
                    .build();
            mBaiduMap.setMyLocationData(myLocationData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG,"onAccuracyChanged");
    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG,"onReceiveLocation");
            // MapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLng = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            myLocationData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())// 设置定位数据的精度信息，单位：米
                    .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(myLocationData);
//            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
            mMyself.lat = location.getLatitude();
            mMyself.lng = location.getLongitude();
            addUserLogo(mMyself);
            if (isFirstLoc) {
                Log.d(TAG,"onReceiveLocation ,isFirstLoc");
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                //mMyself.socketId = SocketIOChannelClient.getInstance().getSocketId();
                mMyself.socketId = WebRtcClient2.getInstance().getSocketId();

                mMyself.lat = ll.latitude;
                mMyself.lng = ll.longitude;
                Log.d(TAG,"onReceiveLocation,socketid:"+mMyself.socketId+",lat:"+mMyself.lat+",lng:"+mMyself.lng);
                //上线
                WebRtcClient2.getInstance().online(ll.latitude,ll.longitude);

            }
        }
    }

    private void addUserLogo(User user){
        if(user == null){
            return;
        }
        Log.d(TAG,"addUserLogo");
        //先删除
        Overlay ov = mOverlays.get(user.socketId);
        if(ov != null){
            ov.remove();
        }

        //定义Maker坐标点
//构建Marker图标
        BitmapDescriptor bitmap ;
        if(user.isGirl){
            bitmap = BitmapDescriptorFactory
                    .fromResource(R.mipmap.girl2);
        }else{
            bitmap = BitmapDescriptorFactory
                    .fromResource(R.mipmap.boy2);
        }
        LatLng point = new LatLng(user.lat,user.lng);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point) //必传参数
                .icon(bitmap) //必传参数
                .draggable(true)
//设置平贴地图，在地图中双指下拉查看效果
                .flat(true)

                ;
//在地图上添加Marker，并显示
        Overlay overlay =mBaiduMap.addOverlay(option);
        mOverlays.put(user.socketId,overlay);
    }

    /**
     * 定位初始化
     */
    public  void initLocation(){
        Log.d(TAG,"initLocation");
        openLocationPermission2();
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        loadMyPost();
        mMapView.setVisibility(View.VISIBLE);

        // 获取传感器管理服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        isLocationLayerEnable = true;
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        // 打开gps
        option.setOpenGps(true);
        // 设置坐标类型
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        setNormalType();
        mBaiduMap.setOnMarkerClickListener(markerClickListener);
    }



    public void setNormalType(){
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 传入null，则为默认图标
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder1 = new MapStatus.Builder();
        builder1.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
    }
    private void openLocationPermission() {
        int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            return;
        }
    }


    private void openLocationPermission2(){
        Log.d(TAG,"openLocationPermission2");
        if(AndPermission.hasPermissions(this, Permission.Group.LOCATION)){

        }else{
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.LOCATION)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //申请权限成功
                            //百度地图初始化
                            SDKInitializer.initialize(getApplicationContext());
                            //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
                            SDKInitializer.setCoordType(CoordType.BD09LL);
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //当用户没有允许该权限时，回调该方法
                            Toast.makeText(MapActivity.this, "没有获取定位权限，该功能无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }

    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserOnline(Users users) {
        Log.d(TAG,"onUserOnline");
        mUsers = users;
        if(mUsers != null && mUsers.peers != null){
            for(User user : mUsers.peers.values()){
                addUserLogo(user);
            }
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotifyJoinRoom(NotifyJoinRoomMsg msg) {
        Log.d(TAG,"onNotifyJoinRoom,from:"+msg.from+",to:"+msg.to+",room:"+msg.room);
        if(!msg.from.equals(msg.to)){
            Intent intent = new Intent();
            intent.putExtra("from",msg.from);
            intent.putExtra("to",msg.to);
            intent.putExtra("room",msg.room);
            intent.putExtra("reason",2);
            Log.d(TAG,"onNotifyJoinRoom , jump to ChatActivity2");
            intent.setClass(MapActivity.this, ChatActivity2.class);
            startActivity(intent);


        }

        //showNormalDialog(msg.from,msg.to,msg.room);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHaveUserOffline(OfflineMsg msg) {
        Log.d(TAG,"onHaveUserOffline,from:"+msg.from);
        //
        if(mUsers != null){
            mUsers.peers.remove(msg.from);
        }
        if(mOverlays != null){
            Overlay ov = mOverlays.get(msg.from);
            if(ov != null){
                ov.remove();
                mOverlays.remove(msg.from);
            }
        }

    }


    BaiduMap.OnMarkerClickListener markerClickListener = new BaiduMap.OnMarkerClickListener() {
        /**
         * 地图 Marker 覆盖物点击事件监听函数
         * @param marker 被点击的 marker
         */
        public boolean onMarkerClick(Marker marker){
            Log.d(TAG,"onMarkerClick");
            LatLng clkPos = marker.getPosition();
            for( User user : mUsers.peers.values()){
                if(clkPos.latitude == user.lat && clkPos.longitude == user.lng){
                    Log.d(TAG,"onMarkerClick,User:"+user.socketId+",lat:"+user.lat+",lng:"+user.lng);
                    if(mMyself != null){
                        if(mMyself.socketId.equals(user.socketId)){
                            Log.d(TAG,"onMarkerClick click myself ,no talk...");
                            Toast.makeText(MapActivity.this,"不能和自己聊天",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.putExtra("from",mMyself.socketId);
                            intent.putExtra("to",user.socketId);
                            intent.putExtra("room",mMyself.socketId);
                            intent.putExtra("reason",1);
                            intent.setClass(MapActivity.this,ChatActivity2.class);
                            startActivity(intent);
                        }else {
                            //点击头像，跳转到聊天页面
                            Intent intent = new Intent();
                            intent.putExtra("from",mMyself.socketId);
                            intent.putExtra("to",user.socketId);
                            intent.putExtra("room",mMyself.socketId);
                            intent.putExtra("reason",1);
                            intent.setClass(MapActivity.this,ChatActivity2.class);
                            startActivity(intent);
                        }

                    }
                }
            }
            return true;
        }
    };


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG,"onKeyUp");
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                long secondTime=System.currentTimeMillis();
                if(secondTime-firstTime>2000){
                    Toast.makeText(MapActivity.this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    if(isListModel){
                        isListModel = !isListModel;
                        usersListView.setVisibility(View.INVISIBLE);
                        switchModel.setBackground(getDrawable(R.mipmap.users));
                    }

                    return true;
                }else{
                    saveMyPos();
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    private void showNormalDialog(String from,String to,String room){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MapActivity.this);
        normalDialog.setTitle("通话请求");
        normalDialog.setMessage("from:"+from+",to:"+to+",room:"+room);
        normalDialog.setPositiveButton("接听",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        Intent intent = new Intent();
                        intent.putExtra("from",from);
                        intent.putExtra("to",to);
                        intent.putExtra("room",room);
                        intent.putExtra("reason",2);
                        intent.setClass(MapActivity.this, ChatActivity2.class);
                        startActivity(intent);

                    }
                });
        normalDialog.setNegativeButton("拒绝",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }

    private void registerNetworkReceiver(){
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
    }
    private void unRegisterNetworkReceiver(){
        unregisterReceiver(netWorkStateReceiver);
    }


    private void saveMyPos(){
        SharedPreferences settings=getSharedPreferences("my_pos",0);
        Log.d(TAG,"save my pos ,lat:"+mCurrentLat+",lng:"+mCurrentLng);
        settings.edit().putString("lat",""+mCurrentLat).commit();
        settings.edit().putString("lng",""+mCurrentLng).commit();
        settings.edit().putString("auc",""+mCurrentAccracy).commit();
        settings.edit().putString("dir",""+mCurrentDirection).commit();
    }
    private void loadMyPost(){
        SharedPreferences settings=getSharedPreferences("my_pos",MODE_PRIVATE);
        String latString = settings.getString("lat","");
        if(latString != null && !latString.isEmpty()){
            mCurrentLat = Double.parseDouble(latString);
        }
        String lngString = settings.getString("lng","");
        if(lngString != null && !lngString.isEmpty()){
            mCurrentLng = Double.parseDouble(lngString);
        }
        String aucString = settings.getString("auc","");
        if(aucString != null && !aucString.isEmpty()){
            mCurrentAccracy = Float.parseFloat(aucString);
        }
        String dirString = settings.getString("dir","");
        if(dirString != null && !dirString.isEmpty()){
            mCurrentDirection =Integer.parseInt(dirString);
        }
        Log.d(TAG,"load my pos:"+mCurrentLat+","+mCurrentLng);
        myLocationData = new MyLocationData.Builder()
                .accuracy(mCurrentAccracy)// 设置定位数据的精度信息，单位：米
                .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                .latitude(mCurrentLat)
                .longitude(mCurrentLng)
                .build();
        mBaiduMap.setMyLocationData(myLocationData);
    }



    class UserAdapter extends ArrayAdapter<User>{
        private int resourceId;
        public UserAdapter(Context context){
            super(context,R.layout.user_item);
            resourceId = R.layout.user_item;
        }


        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            User user=getItem(position); //获取当前项的Fruit实例

            // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
            View view;
            ViewHolder viewHolder;
            if (convertView==null){

                // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
                view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

                // 避免每次调用getView()时都要重新获取控件实例
                viewHolder=new ViewHolder();
                viewHolder.fruitImage=view.findViewById(R.id.user_image);
                viewHolder.fruitName=view.findViewById(R.id.user_name);

                // 将ViewHolder存储在View中（即将控件的实例存储在其中）
                view.setTag(viewHolder);
            } else{
                view=convertView;
                viewHolder=(ViewHolder) view.getTag();
            }

            // 获取控件实例，并调用set...方法使其显示出来
            viewHolder.fruitImage.setImageDrawable(getDrawable(R.mipmap.girl2));
            viewHolder.fruitName.setText(user.socketId);
            return view;
        }

        // 定义一个内部类，用于对控件的实例进行缓存
        class ViewHolder{
            ImageView fruitImage;
            TextView fruitName;
        }

    }


}