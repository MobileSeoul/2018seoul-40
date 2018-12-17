package com.project.mayihelpyou;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPoint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/*
 *  제작자 : Leth(decomposes@naver.com)
 *  사용 api : 카카오맵 api, 카카오지도 uri
 */
public class MapActivity extends FragmentActivity implements  MapView.POIItemEventListener,MapView.MapViewEventListener,MapView.CurrentLocationEventListener
{
    MapView mapView;
    private MapReverseGeoCoder mReverseGeoCoder = null;
    String what_is_it="",name="",Address="",Gu="",OpenTime="",Help="",MoreDiration="",RestRoomStatue="",DisableRestRoom="",
            ExtraRestRoom="",dong="",authority="",area="",operator="",name_kor="",add_kor="",id="",type="",Latitude="",Longitude="";

    boolean ghkwkdtlf,wkddodls,gmqdusqntm,isMapSerach;
    double mLatitude, mLongitude;
    boolean restCheck = false;
    boolean smokeCheck = false;
    boolean selectOne = false;
    boolean directMe = false;
    boolean isDirect = false;
    boolean isSelectOne = false;
    boolean disableCheck = false;
    private AlertDialog dialog;
    double myLatitude;
    double myLongitude;
    String Name, findDong,findGu;
    MapPOIItem mapPOI ,selOnePOI = null;
    TextView tx;

    boolean disables = false;
    int distance = 0;
    String txText = "";
    TextView tv;
    boolean isDefault;
    ImageView iv;

    boolean checkMyLocation = false;
    boolean isLocation = true;
    boolean aa = true;
    double sizeLength = 0.003f;
    boolean isFirst = false;

    int defaultMyTag = 9999999;
    boolean currentLocation = false;
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        currentLocation = true;
        isDefault = true;
       myLatitude = mapPoint.getMapPointGeoCoord().latitude;
       myLongitude = mapPoint.getMapPointGeoCoord().longitude;
       if(isLocation && !selectOne){

         //  mapView.moveCamera(CameraUpdateFactory.newMapPoint(MapPoint.mapPointWithGeoCoord(myLatitude,myLongitude), 1));
           mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(myLatitude,myLongitude),false);
       }

       if(directMe && !checkMyLocation)  helpMe(mapPOI);
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    public class DATA{

        String what_is_it="",name="",Address="",Gu="",OpenTime="",Help="",MoreDiration="",RestRoomStatue="",DisableRestRoom="",
            ExtraRestRoom="",dong="",authority="",area="",operator="",name_kor="",add_kor="",id="",type="";
        double Latitude,Longitude;
        public void setSMOKINGAREA( String authority,String area,String operator, String name_kor,String add_kor,
                 String name,String dong, String id, String type, double Latitude, double Longitude){
            what_is_it = "흡연부스";
            this.authority=authority;
            this.area=area;
            this.operator=operator;
            this.name_kor=name_kor;
            this.add_kor=add_kor;
            this.name = name;
            this.dong=dong;
            this.id=id;
            this.type=type;
            this.Latitude=Latitude;
            this.Longitude=Longitude;
        }


        public void setRESTROOM(String Address, String Gu,String OpenTime, String Help, String MoreDiration,
                            String RestRoomStatue, String DisableRestRoom ,String ExtraRestRoom, String name,
                                String dong, String id, String type, double Latitude, double Longitude){
            this.what_is_it = "화장실";
            this.Address=Address;
            this.Gu=Gu;
            this.OpenTime=OpenTime;
            this.Help=Help;
            this.MoreDiration=MoreDiration;
            this.RestRoomStatue=RestRoomStatue;
            this.DisableRestRoom=DisableRestRoom;
            this.ExtraRestRoom=ExtraRestRoom;
            this.name = name;
            this.dong=dong;
            this.id=id;
            this.type=type;
            this.Latitude=Latitude;
            this.Longitude=Longitude;
        }

        public int distance() {

            double theta = Math.abs(myLongitude - this.Longitude);
            double dist = Math.sin(deg2rad(myLatitude)) * Math.sin(deg2rad(this.Latitude ))
                    + Math.cos(deg2rad(myLatitude)) * Math.cos(deg2rad(this.Latitude)) * Math.cos(deg2rad(theta));

            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1609.344;

            return (int)(dist);
        }


        // This function converts decimal degrees to radians
        public double deg2rad(double deg) {
            return (deg * Math.PI / 180.0);
        }

        // This function converts radians to decimal degrees
        public double rad2deg(double rad) {
            return (rad * 180 / Math.PI);
        }

/*
        public int distance() {
            double d2r = Math.PI / 180;

            double dlong = (Double.valueOf(this.Longitude) - Double.parseDouble(String.format("%.4f", myLongitude))) * d2r;
            double dlat = (Double.valueOf(this.Latitude) - Double.parseDouble(String.format("%.4f", myLatitude))) * d2r;
            double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(Double.parseDouble(String.format("%.4f", myLatitude)) * d2r)
                    * Math.cos(Double.valueOf(this.Latitude) * d2r) * Math.pow(Math.sin(dlong / 2.0), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = 6367 * c * 1000;
            return (int)d;

        }*/

        public String setMessage(){
            String Text = "";
            String tt = distance() > 1000 ? String.format("%d.%dkm",distance()/1000,(distance()%1000)/10) : String.format("%dm",distance());

            if(what_is_it.equals("흡연부스")){
          //      Text = "위치 : " + name_kor + " " + dong + " " + add_kor + "\r\n\r\n";
                if(isDefault)         Text = Text + "거리 : " + tt + "\r\n\r\n";
                Text = Text + "개방 형태 : " + type + "\r\n\r\n";
                Text = Text + "크기 : " + (area.isEmpty() ? "정보없음": area ) + "\r\n\r\n";
                Text = Text + "설치 주체 : " + authority + "\r\n\r\n";
                Text = Text + "관리자 : " + operator + "\r\n\r\n";
            }else{
          //      Text = "이름 : " + name + "\r\n\r\n";
                Text = Text + "위치 : " + Gu + " " + Address + "\r\n\r\n";
                Text = Text + ( MoreDiration.isEmpty() ? "" : "상세주소 : " + MoreDiration+  "\r\n\r\n") ;
                if(isDefault)        Text = Text + "거리 : " + tt + "\r\n\r\n";
                Text = Text + ( type.isEmpty() ? "" : "화장실형태 : " + type+  "\r\n\r\n") ;
                Text = Text +"개방시간 : " + ( OpenTime.isEmpty() ? "정보없음" : OpenTime) +  "\r\n\r\n";
                Text = Text + ( Help.isEmpty() ? "" : Help +  "\r\n\r\n");
                Text = Text +"장애인 화장실 유무 : " + ( DisableRestRoom.isEmpty() ? "없음" : DisableRestRoom) +  "\r\n\r\n";

                if(!RestRoomStatue.isEmpty()){
                    if(RestRoomStatue.indexOf("남녀공용") > -1){
                        Text = Text + "화장실 정보 : 남녀공용\r\n" +
                                "                       " + RestRoomStatue.substring(RestRoomStatue.indexOf(":")+1) + "\r\n";
                    }else {
                        String[] tmp = RestRoomStatue.split("/");
                        Text = Text + "화장실 정보 : " + tmp[0] + "\r\n" +
                                "                       " + tmp[1] + "\r\n";
                    }
                }
                if(!ExtraRestRoom.isEmpty()){
                    String tmp = ExtraRestRoom;
                    ArrayList<String> cctv = new ArrayList<String>();
                    ArrayList<String> child = new ArrayList<String>();
                    ArrayList<String> extra = new ArrayList<String>();
                    ArrayList<String> place = new ArrayList<String>();
                    //Text = Text + "설치 항목\r\n";
                    if(tmp.indexOf("CCTV") > -1)
                        cctv.add("CCTV");
                    if(tmp.indexOf("교환대") > -1)
                        child.add("기저귀교환대");
                    if(tmp.indexOf("조기") > -1)
                        extra.add("손건조기");
                    if(tmp.indexOf("비상벨") > -1)
                        cctv.add("비상벨");
                    if(tmp.indexOf("보호의자") > -1)
                        child.add("유아용보호의자");
                    if(tmp.indexOf("소독기") > -1)
                        extra.add("손소독기");
                    if(tmp.indexOf("종이타올") > -1 || tmp.indexOf("페이퍼타올") > -1)
                        extra.add("종이타올");
                    if(tmp.indexOf("핸드타올") > -1)
                        extra.add("핸드타올");
                    if(tmp.indexOf("난방기") > -1)
                        extra.add("난방기");
                    if(tmp.indexOf("핸드드라이어") > -1)
                        extra.add("핸드드라이어");
                    if(tmp.indexOf("샤워시설") > -1)
                        place.add("샤워시설");
                    if(tmp.indexOf("편의시설") > -1)
                        place.add("편의시설");
                    if(tmp.indexOf("유아용소변기") > -1)
                        child.add("유아용소변기");

                    if(!cctv.isEmpty()) {
                        Text = Text + "보안 : ";
                        for (String temp : cctv) {
                            Text = Text + temp + " ";
                        }
                        Text = Text + "\r\n\r\n";
                    }
                    if(!child.isEmpty()) {
                        Text = Text + "유아관련 : ";
                        for (String temp : child) {
                            Text = Text + temp + " ";
                        }
                        Text = Text + "\r\n\r\n";
                    }
                    if(!place.isEmpty()) {
                        Text = Text + "시설 : ";
                        for (String temp : place) {
                            Text = Text + temp + " ";
                        }
                        Text = Text + "\r\n\r\n";
                    }
                    if(!extra.isEmpty()) {
                        Text = Text + "기타 : ";
                        for (String temp : extra) {
                            Text = Text + temp + " ";
                        }
                        Text = Text + "\r\n\r\n";
                    }
                }
            }
            return Text;
        }
        public String setCaption(){
            String Text = "";
            String t = distance() > 1000 ? String.format("%d.%dkm",distance()/1000,(distance()%1000)/10) : String.format("%dm",distance());
            if(what_is_it.equals("화장실")){
         //       Text = "이름 : " + name + "\r\n\r\n";
                Text = Text + "위치 : " + Gu + " " + Address + "\r\n\r\n";
                Text = Text + ( MoreDiration.isEmpty() ? "" : "상세주소 : " + MoreDiration+  "\r\n\r\n") ;
             if(isDefault)   Text = Text + "거리 : " + t + "\r\n\r\n";
                Text = Text +"개방시간 : " + ( OpenTime.isEmpty() ? "정보없음" : OpenTime) +  "\r\n\r\n";
                Text = Text +"장애인 화장실 유무 : " + ( DisableRestRoom.isEmpty() ? "없음" : DisableRestRoom) +  "\r\n\r\n";
            }else{
            //    Text = "위치 : " + name_kor + " " + dong + " " + add_kor + "\r\n\r\n";
                if(isDefault)      Text = Text + "거리 : " + t+ "\r\n\r\n";
                Text = Text + "개방 형태 : " + type + "\r\n\r\n";
                Text = Text + "크기 : " + (area.isEmpty() ? "정보없음": area ) + "\r\n\r\n";
            }
            return Text;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(directMe) {
                    new AlertDialog.Builder(this)
                            .setMessage("길 안내를 종료하시겠어요?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    notHelpMe(false);
                                }
                            })
                            .setNegativeButton("아니오", null).show();
                }else{
                    finish();
                }
            default:
                return false;
        }
    }
    public void runUiThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        iv.bringToFront();
                        iv.setImageResource(R.color.NaviColor);
                        tx.bringToFront();
                        tx.setText(txText);
                        tv.bringToFront();
                        tv.setText("남은 거리");
                    }
                });
            }
        }).start();
    }
    public void notHelpMe(boolean isNear){
        mapView.removeAllPolylines();
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        findViewById(R.id.nevi).setVisibility(View.GONE);
        findViewById(R.id.textView2).setVisibility(View.GONE);
        findViewById(R.id.textView3).setVisibility(View.GONE);
        findViewById(R.id.imageView).setVisibility(View.GONE);
        mapView.setZoomLevel(1, true);
        if(!selectOne && ! isDirect) {
            findViewById(R.id.smoke).setVisibility(View.VISIBLE);
            findViewById(R.id.restroom).setVisibility(View.VISIBLE);
            findViewById(R.id.disable).setVisibility(View.VISIBLE);
        }
        if(isMapSerach){
            findViewById(R.id.smoke).setVisibility(View.GONE);
            findViewById(R.id.restroom).setVisibility(View.GONE);
            findViewById(R.id.disable).setVisibility(View.GONE);
        }
        aa= true;
        directMe = false;
        if(selectOne)
            mapView.setMapCenterPoint(mapPOI.getMapPoint(),false);
        else
            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(myLatitude,myLongitude),false);
        if(isNear)
            Toast.makeText(getApplicationContext(), "목적지 근처에 도착했습니다. 길안내를 종료합니다.", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(getApplicationContext(), "목적지까지 안내를 취소합니다.", Toast.LENGTH_SHORT).show();
            if (isDirect || isSelectOne)
                finish();
        }
    }
    public void helpMe(MapPOIItem mapPOIItem){
        if(mapPOIItem != null) {
            if(isDefault) {
                findViewById(R.id.nevi).setVisibility(View.VISIBLE);
                findViewById(R.id.smoke).setVisibility(View.GONE);
                findViewById(R.id.restroom).setVisibility(View.GONE);
                findViewById(R.id.disable).setVisibility(View.GONE);
                findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                findViewById(R.id.textView3).setVisibility(View.VISIBLE);
                findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                directMe = true;
                if (mapPOI == null) {
                    mapPOI = mapPOIItem;
                    distance = ((DATA) mapPOI.getUserObject()).distance();
                    if (distance < defaultValue.DEFAULT_NEAR_METER) {
                        txText = String.format("%dm", distance);
                        runUiThread();
                        notHelpMe(true);
                        return;
                    } else {
                        txText = (distance > 1000 ? String.format("%d.%dkm", distance / 1000, (distance % 1000) / 10) : String.format("%dm", distance));
                        runUiThread();
                    }
                } else {
                    if (mapPOI.getTag() == mapPOIItem.getTag()) {
                        if (((DATA) mapPOI.getUserObject()).distance() != distance) {
                            distance = ((DATA) mapPOI.getUserObject()).distance();
                            if (distance < defaultValue.DEFAULT_NEAR_METER) {
                                txText = (distance > 1000 ? String.format("%d.%dkm", distance / 1000, (distance % 1000) / 10) : String.format("%dm", distance));
                                runUiThread();
                                notHelpMe(true);
                                return;
                            } else {
                                txText = (distance > 1000 ? String.format("%d.%dkm", distance / 1000, (distance % 1000) / 10) : String.format("%dm", distance));
                                runUiThread();
                            }
                        }
                    } else {
                        mapPOI = mapPOIItem;
                        distance = ((DATA) mapPOI.getUserObject()).distance();
                        if (distance < defaultValue.DEFAULT_NEAR_METER) {
                            txText = (distance > 1000 ? String.format("%d.%dkm", distance / 1000, (distance % 1000) / 10) : String.format("%dm", distance));
                            runUiThread();
                            notHelpMe(true);
                            return;
                        } else {
                            txText = (distance > 1000 ? String.format("%d.%dkm", distance / 1000, (distance % 1000) / 10) : String.format("%dm", distance));
                            runUiThread();
                        }
                    }
                }
                if (mapView.findPOIItemByTag(mapPOI.getTag()) == null)
                    mapView.addPOIItem(mapPOI);
                mapView.removeAllPolylines();
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
                mapView.selectPOIItem(mapPOI, false);
                MapPolyline polyline = new MapPolyline();
                polyline.setTag(1000);
                polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline 컬러 지정.

// Polyline 좌표 지정.
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(myLatitude, myLongitude));
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(mapPOI.getMapPoint().getMapPointGeoCoord().latitude, mapPOI.getMapPoint().getMapPointGeoCoord().longitude));

// Polyline 지도에 올리기.
                mapView.addPolyline(polyline);
// 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
                if (aa) {

                    mapView.fitMapViewAreaToShowAllPolylines();
                    aa = false;
                }
                runUiThread();
            }else{
                findViewById(R.id.smoke).setVisibility(View.GONE);
                findViewById(R.id.restroom).setVisibility(View.GONE);
                findViewById(R.id.disable).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "시작 위치를 설정해 주세요.", Toast.LENGTH_SHORT).show();
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                directMe = true;
                checkMyLocation = true;
                mapPOI = mapPOIItem;

            }
        }else{
            if(isDefault) {
                findViewById(R.id.smoke).setVisibility(View.GONE);
                findViewById(R.id.restroom).setVisibility(View.GONE);
                findViewById(R.id.disable).setVisibility(View.GONE);
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                Toast.makeText(getApplicationContext(), "근처에 화장실이 없습니다.", Toast.LENGTH_SHORT).show();
                directMe = false;
                mapView.onSurfaceDestroyed();
                mapView = null;
                finish();
            }else{
                findViewById(R.id.smoke).setVisibility(View.GONE);
                findViewById(R.id.restroom).setVisibility(View.GONE);
                findViewById(R.id.disable).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "시작 위치를 설정해 주세요.", Toast.LENGTH_SHORT).show();
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                directMe = true;
                checkMyLocation = true;
                mapView.setZoomLevel(5,false);
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        tx = (TextView)findViewById(R.id.textView2);
        tv = (TextView)findViewById(R.id.textView3);
        iv = findViewById(R.id.imageView);


        boolean internetStat = true;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected())
                    internetStat = true;
            else
                internetStat = false;
        } else
            internetStat = false;

        SharedPreferences pref= getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);

        smokeCheck = pref.getBoolean(defaultValue.SMOKING_AREA_CHECK, false);
        restCheck = pref.getBoolean(defaultValue.RESTROOM_DATA_CHECK, true);
        disableCheck = pref.getBoolean(defaultValue.RESTROOM_DISABLE_DATA_CHECK, false);
        DATA data = new DATA();
        Intent intent = getIntent();  // 넘어온 Intent 객체를 받는다

        isDefault = intent.getBooleanExtra("isDefault",false);
        disables = intent.getBooleanExtra("isDisable",false);
                what_is_it = intent.getStringExtra("what_is_it");
        if(what_is_it != null) {
            if (what_is_it.equals("화장실")) {
                Address = intent.getStringExtra("Address");
                Gu = intent.getStringExtra("Gu");
                OpenTime = intent.getStringExtra("OpenTime");
                Help = intent.getStringExtra("Help");
                MoreDiration = intent.getStringExtra("MoreDiration");
                RestRoomStatue = intent.getStringExtra("RestRoomStatue");
                DisableRestRoom = intent.getStringExtra("DisableRestRoom");
                ExtraRestRoom = intent.getStringExtra("ExtraRestRoom");
            } else if (what_is_it.equals("흡연부스")) {
                authority = intent.getStringExtra("authority");
                area = intent.getStringExtra("area");
                operator = intent.getStringExtra("operator");
                name_kor = intent.getStringExtra("name_kor");
                add_kor = intent.getStringExtra("add_kor");
            }
            name = intent.getStringExtra("name");
            dong = intent.getStringExtra("dong");
            id = intent.getStringExtra("id");
            type = intent.getStringExtra("type");
            Latitude = intent.getStringExtra("Latitude");
            Longitude = intent.getStringExtra("Longitude");

            if (what_is_it.equals("화장실")) {
                data.setRESTROOM(Address, Gu, OpenTime, Help, MoreDiration,
                        RestRoomStatue, DisableRestRoom, ExtraRestRoom, name,
                        dong, id, type, Double.valueOf(Latitude), Double.valueOf(Longitude));
            } else if (what_is_it.equals("흡연부스")) {
                data.setSMOKINGAREA(authority, area, operator, name_kor, add_kor,
                        name, dong, id, type,Double.valueOf(Latitude), Double.valueOf(Longitude));
            }
        }

        directMe = intent.getBooleanExtra("directMe", false); // 키, 디폴트값
        isDirect = directMe;
         myLatitude = intent.getDoubleExtra("myLatitude", defaultValue.DEFAULT_LATITUDE);  // 키, 디폴트값
         myLongitude = intent.getDoubleExtra("myLongitude", defaultValue.DEFAULT_LONGITUDE);  // 키, 디폴트값
        Name = intent.getStringExtra("name");  // 키, 디폴트값
        selectOne =intent.getBooleanExtra("selectOne", false);  // 키, 디폴트값


        ghkwkdtlf =intent.getBooleanExtra("ghkwkdtlf", false);  // 키, 디폴트값
        wkddodls =intent.getBooleanExtra("wkddodls", false);  // 키, 디폴트값
        gmqdusqntm =intent.getBooleanExtra("gmqdusqntm", false);  // 키, 디폴트값
        isMapSerach = intent.getBooleanExtra("isMapSerach", false);  // 키, 디폴트값

       findDong = intent.getStringExtra("dong");
       findGu = intent.getStringExtra("gu");

        isSelectOne = directMe && selectOne;  // 키, 디폴트값
        MapPoint mPoint = MapPoint.mapPointWithGeoCoord(myLatitude, myLongitude);
        mLatitude = myLatitude;
        mLongitude = myLongitude;
        if(mapView != null) {
            mapView.removeAllPOIItems();
            mapView.onSurfaceDestroyed();
            mapView = null;
        }
        mapView = new MapView(this);
        MapView.setMapTilePersistentCacheEnabled(true); // 캐쉬저장
        mapView.setMapViewEventListener(this);
        mapView.setZoomLevel(1, true);
        mapView.setMapCenterPoint(mPoint, true);
        mapView.setCurrentLocationRadius(10);
        mapView.setMapRotationAngle(0,true);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);

        final Button smoke =  findViewById(R.id.smoke);
        final Button disableButton =  findViewById(R.id.disable);
        final Button restroom =  findViewById(R.id.restroom);
        mapView.setPOIItemEventListener(this);
        mapView.setCurrentLocationEventListener(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        smoke.bringToFront();
        disableButton.bringToFront();
        restroom.bringToFront();
        findViewById(R.id.textView2).setVisibility(View.GONE);
        findViewById(R.id.textView3).setVisibility(View.GONE);
        findViewById(R.id.imageView).setVisibility(View.GONE);

        final Button myLocation =  findViewById(R.id.button2);
        myLocation.bringToFront();
        findViewById(R.id.smoke).setVisibility(View.VISIBLE);
        findViewById(R.id.restroom).setVisibility(View.VISIBLE);
        findViewById(R.id.disable).setVisibility(View.VISIBLE);
        findViewById(R.id.button2).setVisibility(View.VISIBLE);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goMyLocation();
            }
        });

        mapView.setShowCurrentLocationMarker(true);
        mapView.setDefaultCurrentLocationMarker();
        smoke.beginBatchEdit();
        isFirst = true;
        Button navi = findViewById(R.id.nevi);
        navi.bringToFront();
        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager pm = getPackageManager();
                PackageInfo pi;

                try {
                    pi = pm.getPackageInfo("net.daum.android.map", PackageManager. GET_ACTIVITIES);
                    notHelpMe(false);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("daummaps://route?sp="+myLatitude+","+myLongitude+"&ep="
                            +mapPOI.getMapPoint().getMapPointGeoCoord().latitude + "," + mapPOI.getMapPoint().getMapPointGeoCoord().longitude +"&by=FOOT"));
                    startActivity(intent);
                }
                catch (PackageManager.NameNotFoundException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    dialog = builder.setMessage("본 기능은 카카오 지도 앱이 깔려있어야 사용이 가능합니다.\r\n 앱을 설치하러 가시겠습니까?")
                            .setTitle("안내")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.daum.android.map"));
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("거부", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .create();
                    dialog.show();

                }
            }
        });
        findViewById(R.id.nevi).setVisibility(View.GONE);
        findViewById(R.id.button2).setVisibility(View.VISIBLE);

        if(!isMapSerach) {
            if (directMe) {
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
                if (selectOne) {
                    MapPOIItem marker = new MapPOIItem();
                    marker.setItemName(name);
                    marker.setTag(Integer.valueOf(id));
                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.valueOf(Latitude), Double.valueOf(Longitude)));
                    if (what_is_it.equals("화장실"))
                        if (Integer.valueOf(id) < defaultValue.RESTROOM_DISABLE_START_INDEX) {
                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                            marker.setCustomImageResourceId(R.drawable.restroom_png);
                            marker.setCustomSelectedImageResourceId(R.drawable.click_restroom_png);
                        } else {
                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                            marker.setCustomImageResourceId(R.drawable.disable_restroom_png);
                            marker.setCustomSelectedImageResourceId(R.drawable.click_disable_restroom_png);
                        }
                    else {
                        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                        marker.setCustomImageResourceId(R.drawable.smoke_png);
                        marker.setCustomSelectedImageResourceId(R.drawable.click_smoke_png);
                    }
                    marker.setUserObject(data);
                    mapView.addPOIItem(marker);
                    helpMe(marker);
                } else {
                    helpMe(findNearMapPOIItem());
                }
            } else if (selectOne) {
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(name);
                marker.setTag(Integer.valueOf(id));
                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.valueOf(Latitude), Double.valueOf(Longitude)));
                if (what_is_it.equals("화장실"))
                    if (Integer.valueOf(id) < defaultValue.RESTROOM_DISABLE_START_INDEX) {
                        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                        marker.setCustomImageResourceId(R.drawable.restroom_png);
                        marker.setCustomSelectedImageResourceId(R.drawable.click_restroom_png);
                    } else {
                        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                        marker.setCustomImageResourceId(R.drawable.disable_restroom_png);
                        marker.setCustomSelectedImageResourceId(R.drawable.click_disable_restroom_png);
                    }
                else {
                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                    marker.setCustomImageResourceId(R.drawable.smoke_png);
                    marker.setCustomSelectedImageResourceId(R.drawable.click_smoke_png);
                }
                marker.setUserObject(data);
                findViewById(R.id.smoke).setVisibility(View.GONE);
                findViewById(R.id.restroom).setVisibility(View.GONE);
                findViewById(R.id.disable).setVisibility(View.GONE);
                mapView.addPOIItem(marker);
                mapView.selectPOIItem(marker, false);
                mPoint = MapPoint.mapPointWithGeoCoord(Double.valueOf(Latitude), Double.valueOf(Longitude));
                mapView.setMapCenterPoint(mPoint, true);
                selOnePOI = marker;
            } else {
                mapView.setShowCurrentLocationMarker(true);
                mapView.setDefaultCurrentLocationMarker();
                if (smokeCheck) showSmokeBox();
                if (smokeCheck) smoke.setBackgroundResource(R.drawable.click_button_smoke_png);
                else smoke.setBackgroundResource(R.drawable.button_smoke_png);

                smoke.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (MotionEvent.ACTION_DOWN == event.getAction() || MotionEvent.ACTION_MOVE == event.getAction()) {
                            smoke.setBackgroundResource(R.drawable.press_button_smoke_png);
                        }
                        if (MotionEvent.ACTION_UP == event.getAction()) {
                            if (smokeCheck == false) {
                                showSmokeBox();
                                smokeCheck = true;
                                smoke.setBackgroundResource(R.drawable.click_button_smoke_png);
                            } else {
                                deleteSmokeBox();
                                smokeCheck = false;
                                smoke.setBackgroundResource(R.drawable.button_smoke_png);
                            }
                            SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreference.edit();
                            //editor.remove(defaultValue.smokeStat);
                            editor.putBoolean(defaultValue.SMOKING_AREA_CHECK, smokeCheck);
                            editor.apply();
                        }
                        if (MotionEvent.ACTION_CANCEL == event.getAction()) {

                            if (smokeCheck == false) {
                                smoke.setBackgroundResource(R.drawable.button_smoke_png);
                            } else {
                                smoke.setBackgroundResource(R.drawable.click_button_smoke_png);
                            }
                        }
                        return true;
                    }
                });
                if (disableCheck && !restCheck) showDisable();

                if (disableCheck && restCheck)
                    disableButton.setBackgroundResource(R.drawable.click_button_disable_restroom_png);
                else disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);

                disableButton.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (MotionEvent.ACTION_DOWN == event.getAction() || MotionEvent.ACTION_MOVE == event.getAction()) {
                            if (restCheck)
                                disableButton.setBackgroundResource(R.drawable.press_button_disable_restroom_png);
                        }
                        if (MotionEvent.ACTION_UP == event.getAction()) {
                            if (restCheck) {
                                if (disableCheck == false) {
                                    disableCheck = true;
                                    showDisable();
                                    disableButton.setBackgroundResource(R.drawable.click_button_disable_restroom_png);
                                } else {
                                    disableCheck = false;
                                    deleteDisable();
                                    disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                                }
                            } else
                                disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                            SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreference.edit();
                            //editor.remove(defaultValue.restRoomStat);
                            editor.putBoolean(defaultValue.RESTROOM_DISABLE_DATA_CHECK, disableCheck);
                            editor.apply();
                        }
                        if (MotionEvent.ACTION_CANCEL == event.getAction()) {

                            if (disables == false) {
                                disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                            } else if (restCheck) {
                                disableButton.setBackgroundResource(R.drawable.click_button_disable_restroom_png);
                            } else {
                                disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                            }
                        }
                        return true;
                    }
                });
            /*
            disableButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(restCheck){
                        if (disableCheck == false) {
                            disableCheck = true;
                            showDisable();
                            disableButton.setBackgroundResource(R.drawable.click_button_disable_restroom_png);
                        } else {
                            disableCheck = false;
                            deleteDisable();
                            disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                        }
                    }else disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                    SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreference.edit();
                    //editor.remove(defaultValue.restRoomStat);
                    editor.putBoolean(defaultValue.RESTROOM_DISABLE_DATA_CHECK, disableCheck);
                    editor.apply();
                }
            });*/

                if (restCheck) showRestRoom();
                if (restCheck) restroom.setBackgroundResource(R.drawable.click_button_restroom_png);
                else restroom.setBackgroundResource(R.drawable.button_restroom_png);

                restroom.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (MotionEvent.ACTION_DOWN == event.getAction() || MotionEvent.ACTION_MOVE == event.getAction()) {
                            restroom.setBackgroundResource(R.drawable.press_button_restroom_png);
                        }
                        if (MotionEvent.ACTION_UP == event.getAction()) {
                            if (restCheck == false) {
                                if (disableCheck)
                                    disableButton.setBackgroundResource(R.drawable.click_button_disable_restroom_png);
                                else
                                    disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                                showRestRoom();
                                restroom.setBackgroundResource(R.drawable.click_button_restroom_png);
                                restCheck = true;
                            } else {
                                deleteRestRoom();
                                restroom.setBackgroundResource(R.drawable.button_restroom_png);
                                disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                                restCheck = false;
                            }
                            SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreference.edit();
                            //editor.remove(defaultValue.restRoomStat);
                            editor.putBoolean(defaultValue.RESTROOM_DATA_CHECK, restCheck);
                            editor.commit();
                        }
                        if (MotionEvent.ACTION_CANCEL == event.getAction()) {

                            if (restCheck == false) {
                                restroom.setBackgroundResource(R.drawable.button_restroom_png);
                            } else {
                                restroom.setBackgroundResource(R.drawable.click_button_restroom_png);
                            }
                        }
                        return true;
                    }
                });
                restroom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (restCheck == false) {
                            if (disableCheck)
                                disableButton.setBackgroundResource(R.drawable.click_button_disable_restroom_png);
                            else
                                disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                            showRestRoom();
                            restroom.setBackgroundResource(R.drawable.click_button_restroom_png);
                            restCheck = true;
                        } else {
                            deleteRestRoom();
                            restroom.setBackgroundResource(R.drawable.button_restroom_png);
                            disableButton.setBackgroundResource(R.drawable.button_disable_restroom_png);
                            restCheck = false;
                        }
                        SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreference.edit();
                        //editor.remove(defaultValue.restRoomStat);
                        editor.putBoolean(defaultValue.RESTROOM_DATA_CHECK, restCheck);
                        editor.commit();
                    }
                });


            }
        }else{
            findViewById(R.id.smoke).setVisibility(View.GONE);
            findViewById(R.id.restroom).setVisibility(View.GONE);
            findViewById(R.id.disable).setVisibility(View.GONE);
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(myLatitude,myLongitude),false);

            if(ghkwkdtlf){
                try{
                    SQLiteDatabase sampleDB;
                    sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
                    Cursor c;
                    if(findGu.equals("전체"))
                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME, null);
                    else if(findDong.equals("전체"))
                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME +
                                " WHERE gu ='"+findGu +"'" , null);
                    else
                    c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME +
                            " WHERE gu ='"+findGu +"' and dong='"+findDong + "'" , null);
                    if(c.moveToFirst()) {
                        for(;;) {
                            if(mapView.findPOIItemByTag(c.getInt(0)) == null) {
                                MapPOIItem marker = new MapPOIItem();
                                marker.setItemName(c.getString(1));
                                marker.setTag(c.getInt(0));
                                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(c.getDouble(2), c.getDouble(3)));

                                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                                    marker.setCustomImageResourceId(R.drawable.restroom_png);
                                    marker.setCustomSelectedImageResourceId(R.drawable.click_restroom_png);

                                DATA temp = new DATA();
                                temp.setRESTROOM( c.getString(4), c.getString(5), c.getString(7),  c.getString(8),c.getString(9),
                                        c.getString(10),  c.getString(11) , c.getString(12), c.getString(1),
                                        c.getString(13),  c.getString(0),  c.getString(6),  c.getDouble(2),  c.getDouble(3));

                                marker.setUserObject(temp);
                                mapView.addPOIItem(marker);
                            }
                            if(!c.moveToNext()) break;
                        }
                    }
                    sampleDB.close();
                    c.close();
                }catch (SQLiteException e){
                }



            }else if(wkddodls){

                try{
                    SQLiteDatabase sampleDB;
                    sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
                    Cursor c ;
                    if(findGu.equals("전체"))
                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME +
                            " WHERE id >= " + defaultValue.RESTROOM_DISABLE_START_INDEX , null);

                    else if(findDong.equals("전체"))
                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME +
                                " WHERE gu ='"+findGu +"'" , null);
                    else
                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME +
                                " WHERE gu ='"+findGu +"' and dong='"+findDong + "' and id >= " + defaultValue.RESTROOM_DISABLE_START_INDEX  , null);

                    if(c.moveToFirst()) {
                        for(;;) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(c.getString(1));
                            marker.setTag(c.getInt(0));
                            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(c.getDouble(2), c.getDouble(3)));
                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                            marker.setCustomImageResourceId(R.drawable.disable_restroom_png);
                            marker.setCustomSelectedImageResourceId(R.drawable.click_disable_restroom_png);

                            DATA temp = new DATA();
                            temp.setRESTROOM( c.getString(4), c.getString(5), c.getString(7),  c.getString(8),c.getString(9),
                                    c.getString(10),  c.getString(11) , c.getString(12), c.getString(1),
                                    c.getString(13),  c.getString(0),  c.getString(6),  c.getDouble(2),  c.getDouble(3));

                            marker.setUserObject(temp);
                            if(mapView.findPOIItemByTag(marker.getTag())!=null) {
                                mapView.removePOIItem(mapView.findPOIItemByTag(marker.getTag()));
                            }
                            mapView.addPOIItem(marker);
                            if(!c.moveToNext()) break;
                        }
                    }
                    sampleDB.close();
                    c.close();
                }catch (SQLiteException e){
                }

            }else if(gmqdusqntm){
                try{
                    SQLiteDatabase sampleDB;
                    sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE).toString(), MODE_PRIVATE, null);
                    Cursor c;

                    if(findGu.equals("전체"))
                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME, null);
                    else if(findDong.equals("전체"))
                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME + " WHERE (name_kor='" + findGu + "')" , null);
                    else
                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME + " WHERE (name_kor='" + findGu + "' And dong = '"+ findDong + "')" , null);

                    if(c.moveToFirst()) {
                        for(;;) {
                            if(mapView.findPOIItemByTag(c.getInt(0)) == null) {
                                MapPOIItem marker = new MapPOIItem();
                                marker.setItemName(c.getString(4));
                                marker.setTag(c.getInt(0));
                                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(c.getDouble(7), c.getDouble(8)));
                                //  marker.setMarkerType(MapPOIItem.MarkerType.YellowPin); // 기본으로 제공하는 BluePin 마커 모양.
                                // marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setCustomImageResourceId(R.drawable.smoke_png);
                                marker.setCustomSelectedImageResourceId(R.drawable.click_smoke_png);

                                DATA temp = new DATA();
                                temp.setSMOKINGAREA(  c.getString(1), c.getString(2), c.getString(4),  c.getString(5), c.getString(6),
                                        (c.getString(5)+" " + c.getString(9) + " " +c.getString(6)), c.getString(9),  c.getString(0),  c.getString(3), c.getDouble(7),c.getDouble(8));
                                marker.setUserObject(temp);
                                mapView.addPOIItem(marker);
                            }
                            if(!c.moveToNext()) break;
                        }
                    }
                    sampleDB.close();
                    c.close();
                }catch (SQLiteException e){}
            }







        }

        if(!internetStat){
            Toast.makeText(getApplicationContext(), "인터넷이 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
            if(mapView != null)
                mapView.onSurfaceDestroyed();
            mapView = null;
            finish();
        }

    }


    public MapPOIItem findNearMapPOIItem(){
        ArrayList<MapPOIItem> arrayList = new ArrayList<>();
        int i = 0;
        int near = 1000000;
        if(isDefault) {
            try {
                SQLiteDatabase sampleDB;
                sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
                Cursor c;
                if (!disables)
                    c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME + " WHERE (Latitude BETWEEN " + (myLatitude - (0.006f)) + " AND " + (myLatitude + 0.006f) + ") AND (Longitude BETWEEN " + (myLongitude - 0.006f) + " AND " + (myLongitude + 0.006f) + ")", null);
                else
                    c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME + " WHERE (Latitude BETWEEN " + (myLatitude - (0.006f)) + " AND " + (myLatitude + (0.006f)) + ") AND (Longitude BETWEEN " + (myLongitude - (0.006f)) + " AND " + (myLongitude + (0.006f)) + ") and id >= " + defaultValue.RESTROOM_DISABLE_START_INDEX, null);

                if (c != null) {
                    if (c.moveToFirst()) {
                        for (int count = 0; ; count++) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(c.getString(1));
                            marker.setTag(c.getInt(0));
                            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(c.getDouble(2), c.getDouble(3)));

                            if (disables) {
                                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setCustomImageResourceId(R.drawable.disable_restroom_png);
                                marker.setCustomSelectedImageResourceId(R.drawable.click_disable_restroom_png);

                            } else {
                                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setCustomImageResourceId(R.drawable.restroom_png);
                                marker.setCustomSelectedImageResourceId(R.drawable.click_restroom_png);
                            }

                            DATA temp = new DATA();
                            temp.setRESTROOM(c.getString(4), c.getString(5), c.getString(7), c.getString(8), c.getString(9),
                                    c.getString(10), c.getString(11), c.getString(12), c.getString(1),
                                    c.getString(13), c.getString(0), c.getString(6), c.getDouble(2), c.getDouble(3));

                            marker.setUserObject(temp);
                            arrayList.add(marker);
                            int tmp = temp.distance();
                            if (tmp < near) {
                                i = count;
                                near = tmp;
                            }
                            if (!c.moveToNext()) break;
                        }
                        c.close();
                        sampleDB.close();
                        return arrayList.get(i);
                    }
                } else {
                    return null;
                }
            } catch (SQLiteException e) {
            }
        }

        return null;

    }

    public void showRestRoom(){
            try{
            SQLiteDatabase sampleDB;
                sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
                Cursor c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME +
                        " WHERE (Latitude BETWEEN " + (mLatitude - sizeLength)
                        + " AND " + (mLatitude + sizeLength) +
                        ") AND (Longitude BETWEEN " + (mLongitude - sizeLength) +
                        " AND " + (mLongitude + sizeLength)+")" , null);
            if(c.moveToFirst()) {
                    for(;;) {
                        if(mapView.findPOIItemByTag(c.getInt(0)) == null) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(c.getString(1));
                            marker.setTag(c.getInt(0));
                            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(c.getDouble(2), c.getDouble(3)));
                            if(disableCheck && c.getInt(0) >= defaultValue.RESTROOM_DISABLE_START_INDEX){

                                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setCustomImageResourceId(R.drawable.disable_restroom_png);
                                marker.setCustomSelectedImageResourceId(R.drawable.click_disable_restroom_png);
                            }else{
                                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                                marker.setCustomImageResourceId(R.drawable.restroom_png);
                                marker.setCustomSelectedImageResourceId(R.drawable.click_restroom_png);
                            }

                            DATA temp = new DATA();
                            temp.setRESTROOM( c.getString(4), c.getString(5), c.getString(7),  c.getString(8),c.getString(9),
                                    c.getString(10),  c.getString(11) , c.getString(12), c.getString(1),
                                    c.getString(13),  c.getString(0),  c.getString(6),  c.getDouble(2),  c.getDouble(3));

                            marker.setUserObject(temp);
                            mapView.addPOIItem(marker);
                        }
                        if(!c.moveToNext()) break;
                    }
                }
            sampleDB.close();
            c.close();
        }catch (SQLiteException e){
         }
    }

    public void showDisable(){
        try{
            SQLiteDatabase sampleDB;
            sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
            Cursor c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME +
                    " WHERE (Latitude BETWEEN " + (mLatitude - sizeLength)
                    + " AND " + (mLatitude + sizeLength)
                    + ") AND (Longitude BETWEEN " + (mLongitude - sizeLength)
                    + " AND " + (mLongitude + sizeLength)+") and id >= " + defaultValue.RESTROOM_DISABLE_START_INDEX , null);

            if(c.moveToFirst()) {
                for(;;) {
                        MapPOIItem marker = new MapPOIItem();
                        marker.setItemName(c.getString(1));
                        marker.setTag(c.getInt(0));
                        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(c.getDouble(2), c.getDouble(3)));
                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                    marker.setCustomImageResourceId(R.drawable.disable_restroom_png);
                    marker.setCustomSelectedImageResourceId(R.drawable.click_disable_restroom_png);

                        DATA temp = new DATA();
                        temp.setRESTROOM( c.getString(4), c.getString(5), c.getString(7),  c.getString(8),c.getString(9),
                                c.getString(10),  c.getString(11) , c.getString(12), c.getString(1),
                                c.getString(13),  c.getString(0),  c.getString(6),  c.getDouble(2),  c.getDouble(3));

                        marker.setUserObject(temp);
                        if(mapView.findPOIItemByTag(marker.getTag())!=null) {
                            mapView.removePOIItem(mapView.findPOIItemByTag(marker.getTag()));
                        }
                         mapView.addPOIItem(marker);
                    if(!c.moveToNext()) break;
                }
            }
            sampleDB.close();
            c.close();
        }catch (SQLiteException e){
        }
    }

    public void deleteDisable(){
        for(MapPOIItem poiItem :  mapView.getPOIItems()){
           if(poiItem.getTag() >= defaultValue.RESTROOM_DISABLE_START_INDEX) {
               MapPOIItem pp = poiItem;
               pp.setMarkerType(MapPOIItem.MarkerType.CustomImage);
               pp.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
               pp.setCustomImageResourceId(R.drawable.restroom_png);
               pp.setCustomSelectedImageResourceId(R.drawable.click_restroom_png);
               mapView.removePOIItem(poiItem);
               mapView.addPOIItem(pp);
           }
        }
        showRestRoom();
    }
    public void showSmokeBox(){
        try{
            SQLiteDatabase sampleDB;
            sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE).toString(), MODE_PRIVATE, null);
            Cursor c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME + " WHERE (Latitude BETWEEN " + (mLatitude - sizeLength)
                    + " AND " + (mLatitude + sizeLength)
                    + ") AND (Longitude BETWEEN "
                    + (mLongitude - sizeLength) + " AND " + (mLongitude + sizeLength)+")" , null);
            if(c.moveToFirst()) {
                for(;;) {
                    if(mapView.findPOIItemByTag(c.getInt(0)) == null) {
                        MapPOIItem marker = new MapPOIItem();
                        marker.setItemName(c.getString(4));
                        marker.setTag(c.getInt(0));
                        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(c.getDouble(7), c.getDouble(8)));
                      //  marker.setMarkerType(MapPOIItem.MarkerType.YellowPin); // 기본으로 제공하는 BluePin 마커 모양.
                       // marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                       marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                        marker.setCustomImageResourceId(R.drawable.smoke_png);
                        marker.setCustomSelectedImageResourceId(R.drawable.click_smoke_png);

                        DATA temp = new DATA();
                        temp.setSMOKINGAREA(  c.getString(1), c.getString(2), c.getString(4),  c.getString(5), c.getString(6),
                                (c.getString(5)+" " + c.getString(9) + " " +c.getString(6)), c.getString(9),  c.getString(0),  c.getString(3), c.getDouble(7),c.getDouble(8));
                        marker.setUserObject(temp);
                        mapView.addPOIItem(marker);
                    }
                    if(!c.moveToNext()) break;
                }
            }
            sampleDB.close();
            c.close();
        }catch (SQLiteException e){}
    }

    public void deleteSmokeBox(){
        for(MapPOIItem poiItem :  mapView.getPOIItems()){
            if(poiItem.getTag() < defaultValue.RESTROOM_START_INDEX )
                mapView.removePOIItem(poiItem);
        }
    }

    public void deleteRestRoom(){
        for(MapPOIItem poiItem :  mapView.getPOIItems()){
                if (poiItem.getTag() >= defaultValue.RESTROOM_START_INDEX )
                mapView.removePOIItem(poiItem);
        }
    }

    public void goMyLocation(){
        if(isDefault || selectOne) {
            if (selectOne && !directMe) {
                mapView.moveCamera(CameraUpdateFactory.newMapPoint(MapPoint.mapPointWithGeoCoord(selOnePOI.getMapPoint().getMapPointGeoCoord().latitude, selOnePOI.getMapPoint().getMapPointGeoCoord().longitude), 1));
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(selOnePOI.getMapPoint().getMapPointGeoCoord().latitude, selOnePOI.getMapPoint().getMapPointGeoCoord().longitude), false);

            } else {
                mapView.moveCamera(CameraUpdateFactory.newMapPoint(MapPoint.mapPointWithGeoCoord(myLatitude, myLongitude), 1));
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(myLatitude, myLongitude), false);
                isLocation = true;
            }
        }else{
            Toast.makeText(getApplicationContext(), "시작 위치를 설정해 주세요.", Toast.LENGTH_SHORT).show();
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            checkMyLocation = true;

        }
    }

    // MapView.POIItemEventListener ======================================================================================
    @Override
    public void onPOIItemSelected(final MapView mapView, MapPOIItem poiItem){
        final MapPOIItem mapPOIItem = poiItem;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        if(mapPOIItem.getTag() == defaultMyTag)
            if(checkMyLocation) {
            dialog = builder.setMessage("이 위치를 시작 위치로 하시겠습니까?")
                    .setTitle(MapReverseGeoCoder.findAddressForMapPoint("2eeecd3c13467e4619ded97b80a7d55f", mapPOIItem.getMapPoint()))
                    .setNegativeButton("취소", null)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myLatitude = mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude;
                            myLongitude = mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude;
                            isDefault = true;
                            checkMyLocation = false;
                            goMyLocation();
                            if(directMe && !selectOne && mapPOI == null)
                                helpMe(findNearMapPOIItem());
                            else if(directMe)
                                 helpMe(mapPOI);
                        }
                    })
                    .create();
            dialog.show();
        }else{
                dialog = builder.setMessage("시작 위치 등록을 취소하시겠습니까?")
                        .setTitle(MapReverseGeoCoder.findAddressForMapPoint("2eeecd3c13467e4619ded97b80a7d55f", mapPOIItem.getMapPoint()))
                        .setNegativeButton("취소", null)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isDefault = false;
                                checkMyLocation = false;
                                if(mapView.findPOIItemByTag(defaultMyTag) != null) mapView.removePOIItem(mapView.findPOIItemByTag(defaultMyTag));
                                if(directMe)
                                    notHelpMe(false);
                            }
                        })
                        .create();
                dialog.show();
            }
    }
    public void removeMyLocation(){
        if(mapView.findPOIItemByTag(defaultMyTag) != null) mapView.removePOIItem(mapView.findPOIItemByTag(defaultMyTag));
    }
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        final MapPOIItem poiItem = mapPOIItem;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);

        if(mapPOIItem.getTag() == defaultMyTag){
            if(checkMyLocation) {
                dialog = builder.setMessage("이 위치를 시작 위치로 하시겠습니까?")
                        .setTitle(MapReverseGeoCoder.findAddressForMapPoint("2eeecd3c13467e4619ded97b80a7d55f", mapPOIItem.getMapPoint()))
                        .setNegativeButton("취소", null)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myLatitude = poiItem.getMapPoint().getMapPointGeoCoord().latitude;
                                myLongitude = poiItem.getMapPoint().getMapPointGeoCoord().longitude;
                                isDefault = true;
                                checkMyLocation = false;
                                goMyLocation();
                                if(directMe && !selectOne && mapPOI == null)
                                    helpMe(findNearMapPOIItem());
                                else if(directMe)
                                    helpMe(mapPOI);
                            }
                        })
                        .create();
                dialog.show();
            }else{
                dialog = builder.setMessage("시작 위치 등록을 취소하시겠습니까?")
                        .setTitle(MapReverseGeoCoder.findAddressForMapPoint("2eeecd3c13467e4619ded97b80a7d55f", mapPOIItem.getMapPoint()))
                        .setNegativeButton("취소", null)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isDefault = false;
                                checkMyLocation = false;
                                removeMyLocation();
                                if(directMe)
                                    notHelpMe(false);
                            }
                        })
                        .create();
                dialog.show();
            }
        }else {
            dialog = builder.setMessage((mapPOIItem.getUserObject() == null ? "자세한 정보가 존재하지 않습니다." : ((DATA) mapPOIItem.getUserObject()).setCaption()))
                    .setTitle(mapPOIItem.getItemName())
                    .setNegativeButton((mapPOI != null && mapPOI.getTag() == mapPOIItem.getTag()) && directMe ? "길안내 취소" : "길안내", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dd, int whichButton) {
                            if ((mapPOI != null && mapPOI.getTag() == poiItem.getTag()) && directMe)
                                notHelpMe(false);
                            else
                                helpMe(poiItem);
                        }
                    })
                    .setNeutralButton("상세설명", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dd, int whichButton) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                            dialog = builder.setMessage((poiItem.getUserObject() == null ? "자세한 정보가 존재하지 않습니다." : ((DATA) poiItem.getUserObject()).setMessage()))
                                    .setTitle(poiItem.getItemName())
                                    .setNeutralButton("로드뷰", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dd, int whichButton) {
                                            PackageManager pm = getPackageManager();
                                            PackageInfo pi;

                                            try {
                                                pi = pm.getPackageInfo("net.daum.android.map", PackageManager.GET_ACTIVITIES);
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("daummaps://roadView?p=" + poiItem.getMapPoint().getMapPointGeoCoord().latitude + "," + poiItem.getMapPoint().getMapPointGeoCoord().longitude));
                                                startActivity(intent);
                                            } catch (PackageManager.NameNotFoundException e) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                                                dialog = builder.setMessage("본 기능은 카카오 지도 앱이 깔려있어야 사용이 가능합니다.\r\n 앱을 설치하러 가시겠습니까?")
                                                        .setTitle("안내")
                                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.daum.android.map"));
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setNegativeButton("거부", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                            }
                                                        })
                                                        .create();
                                                dialog.show();
                                            }
                                        }
                                    })
                                    .setPositiveButton("확인", null)
                                    .create();
                            dialog.show();
                        }
                    })
                    .setPositiveButton("확인", null)
                    .create();
            dialog.show();
        }
    }
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {}
    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {}
    @Override
    public void onMapViewInitialized(MapView mapView) {}
    // MapView.MapViewEventListener ======================================================================================
    @Override  // 중심좌표 이동
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
     //   if(i > 5)
      //      mapView.setZoomLevel(5,false);

        /*
        if( i > 0 && i < 6)
            sizeLength = 0.003f *  3 * i;
        else if( i > 5)
            sizeLength = 0.06f;
        else
            sizeLength = 0.003f;*/


    }
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        if(checkMyLocation){
            if(mapView.findPOIItemByTag(defaultMyTag) != null) mapView.removePOIItem(mapView.findPOIItemByTag(defaultMyTag));
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName("내위치");
            marker.setTag(defaultMyTag);
            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(mapPoint.getMapPointGeoCoord().latitude,mapPoint.getMapPointGeoCoord().longitude));
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            marker.setCustomImageResourceId(R.drawable.now_location);
            marker.setCustomSelectedImageResourceId(R.drawable.now_location);
            mapView.addPOIItem(marker);
        }
    }


    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {}

    @Override  // 맵 이동 완료시
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        if (!isMapSerach) {
            if (Math.abs(mapPoint.getMapPointGeoCoord().latitude - myLatitude) < 0.0003f ||
                    Math.abs(mapPoint.getMapPointGeoCoord().longitude - myLongitude) < 0.0003f)
                isLocation = true;
            else
                isLocation = false;
            if (directMe == false) {
                if (selectOne == false) {
                    if (Math.abs(mapPoint.getMapPointGeoCoord().latitude - mLatitude) > sizeLength / 2 || Math.abs(mapPoint.getMapPointGeoCoord().longitude - mLongitude) > sizeLength / 2) {
                        mLatitude = mapPoint.getMapPointGeoCoord().latitude;
                        mLongitude = mapPoint.getMapPointGeoCoord().longitude;
                        if (restCheck)
                            showRestRoom();
                        if (!restCheck && disableCheck)
                            showDisable();
                        if (smokeCheck)
                            showSmokeBox();
                    }
                }
            } else {
                //  myLatitude = mapPoint.getMapPointGeoCoord().latitude;
                //  myLongitude = mapPoint.getMapPointGeoCoord().longitude;
                mLatitude = mapPoint.getMapPointGeoCoord().latitude;
                mLongitude = mapPoint.getMapPointGeoCoord().longitude;
                if (!checkMyLocation) helpMe(mapPOI);
            }
        }
    }
}

