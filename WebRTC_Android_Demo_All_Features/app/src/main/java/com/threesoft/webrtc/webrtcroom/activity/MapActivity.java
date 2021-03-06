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
    // ????????????
    private LocationClient mLocClient;
    private MyLocationListener myListener = new MyLocationListener();
    // ????????????????????????
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private float mCurrentAccracy;
    // ???????????????
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    // ??????????????????
    private boolean isFirstLoc = true;
    // ????????????????????????
    private boolean isLocationLayerEnable = true;
    private MyLocationData myLocationData;
//    private ArrayList<User> mUsers = new ArrayList<>();
    private Users mUsers ;
    private HashMap<String, Overlay> mOverlays = new HashMap<>();
    private User mMyself = new User();
    private double mCurrentLat = 0.0;
    private double mCurrentLng = 0.0;

    //??????????????????????????????????????????
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
        // ??????????????????????????????
        //??????????????????????????????
        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if (!Settings.canDrawOverlays(this)) {
                //???????????????????????????
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

        //??????
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
                        Toast.makeText(MapActivity.this,"?????????????????????",Toast.LENGTH_LONG).show();
                    }else {
                        //????????????????????????????????????
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
        // ???activity??????onResume???????????????mMapView. onResume ()
        mMapView.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        // ???activity??????onPause???????????????mMapView. onPause ()
        mMapView.onPause();
        unRegisterNetworkReceiver();
        super.onPause();

    }


    @Override
    protected void onDestroy() {

        Log.d(TAG,"onDestroy");
        // ???????????????????????????
        mSensorManager.unregisterListener(this);
        // ?????????????????????
        mLocClient.stop();
        // ??????????????????
        mBaiduMap.setMyLocationEnabled(false);
        // ???activity??????onDestroy???????????????mMapView.onDestroy()
        mMapView.onDestroy();
        //???????????? , ??????Activity????????????
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
                    .accuracy(mCurrentAccracy)// ????????????????????????????????????????????????
                    .direction(mCurrentDirection)// ?????????????????????????????????????????????????????????0-360
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
     * ??????SDK????????????
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG,"onReceiveLocation");
            // MapView ???????????????????????????????????????
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLng = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            myLocationData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())// ????????????????????????????????????????????????
                    .direction(mCurrentDirection)// ?????????????????????????????????????????????????????????0-360
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
                //??????
                WebRtcClient2.getInstance().online(ll.latitude,ll.longitude);

            }
        }
    }

    private void addUserLogo(User user){
        if(user == null){
            return;
        }
        Log.d(TAG,"addUserLogo");
        //?????????
        Overlay ov = mOverlays.get(user.socketId);
        if(ov != null){
            ov.remove();
        }

        //??????Maker?????????
//??????Marker??????
        BitmapDescriptor bitmap ;
        if(user.isGirl){
            bitmap = BitmapDescriptorFactory
                    .fromResource(R.mipmap.girl2);
        }else{
            bitmap = BitmapDescriptorFactory
                    .fromResource(R.mipmap.boy2);
        }
        LatLng point = new LatLng(user.lat,user.lng);
//??????MarkerOption???????????????????????????Marker
        OverlayOptions option = new MarkerOptions()
                .position(point) //????????????
                .icon(bitmap) //????????????
                .draggable(true)
//?????????????????????????????????????????????????????????
                .flat(true)

                ;
//??????????????????Marker????????????
        Overlay overlay =mBaiduMap.addOverlay(option);
        mOverlays.put(user.socketId,overlay);
    }

    /**
     * ???????????????
     */
    public  void initLocation(){
        Log.d(TAG,"initLocation");
        openLocationPermission2();
        // ???????????????
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        loadMyPost();
        mMapView.setVisibility(View.VISIBLE);

        // ???????????????????????????
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // ??????????????????????????????????????????
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);

        // ??????????????????
        mBaiduMap.setMyLocationEnabled(true);
        isLocationLayerEnable = true;
        // ???????????????
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        // ??????gps
        option.setOpenGps(true);
        // ??????????????????
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        setNormalType();
        mBaiduMap.setOnMarkerClickListener(markerClickListener);
    }



    public void setNormalType(){
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // ??????null?????????????????????
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
                            //??????????????????
                            //?????????????????????
                            SDKInitializer.initialize(getApplicationContext());
                            //??????BD09LL???GCJ02????????????????????????BD09LL?????????
                            SDKInitializer.setCoordType(CoordType.BD09LL);
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //???????????????????????????????????????????????????
                            Toast.makeText(MapActivity.this, "????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
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
         * ?????? Marker ?????????????????????????????????
         * @param marker ???????????? marker
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
                            Toast.makeText(MapActivity.this,"?????????????????????",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.putExtra("from",mMyself.socketId);
                            intent.putExtra("to",user.socketId);
                            intent.putExtra("room",mMyself.socketId);
                            intent.putExtra("reason",1);
                            intent.setClass(MapActivity.this,ChatActivity2.class);
                            startActivity(intent);
                        }else {
                            //????????????????????????????????????
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
                    Toast.makeText(MapActivity.this,"????????????????????????",Toast.LENGTH_SHORT).show();
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
        /* @setIcon ?????????????????????
         * @setTitle ?????????????????????
         * @setMessage ???????????????????????????
         * setXXX????????????Dialog???????????????????????????????????????
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MapActivity.this);
        normalDialog.setTitle("????????????");
        normalDialog.setMessage("from:"+from+",to:"+to+",room:"+room);
        normalDialog.setPositiveButton("??????",
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
        normalDialog.setNegativeButton("??????",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // ??????
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
                .accuracy(mCurrentAccracy)// ????????????????????????????????????????????????
                .direction(mCurrentDirection)// ?????????????????????????????????????????????????????????0-360
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
            User user=getItem(position); //??????????????????Fruit??????

            // ?????????????????????ListView???????????????????????????????????????????????????????????????
            View view;
            ViewHolder viewHolder;
            if (convertView==null){

                // ??????ListView???????????????????????????????????????????????????????????????
                view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

                // ??????????????????getView()?????????????????????????????????
                viewHolder=new ViewHolder();
                viewHolder.fruitImage=view.findViewById(R.id.user_image);
                viewHolder.fruitName=view.findViewById(R.id.user_name);

                // ???ViewHolder?????????View?????????????????????????????????????????????
                view.setTag(viewHolder);
            } else{
                view=convertView;
                viewHolder=(ViewHolder) view.getTag();
            }

            // ??????????????????????????????set...????????????????????????
            viewHolder.fruitImage.setImageDrawable(getDrawable(R.mipmap.girl2));
            viewHolder.fruitName.setText(user.socketId);
            return view;
        }

        // ????????????????????????????????????????????????????????????
        class ViewHolder{
            ImageView fruitImage;
            TextView fruitName;
        }

    }


}