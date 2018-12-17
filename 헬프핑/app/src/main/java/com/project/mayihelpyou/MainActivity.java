package com.project.mayihelpyou;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class MainActivity extends FragmentActivity {
    double myLatitude = defaultValue.DEFAULT_LATITUDE;
    double myLongitude = defaultValue.DEFAULT_LONGITUDE;
    TextView tx;
    LocationManager lm;
    boolean isGPSEnabled, isNetworkEnabled;
    String strLanguage;
    Geocoder geocoder;
    String addressLine;
    boolean isGetGPS = false;
    boolean isDefault = false;

    /*
    // 원래 언어에 따라서 할려 햇으나 화장실 데이터가 나머지가 없음..
    // SmokeData -> Ko, en, zh
    // RestRoom -> Ko
     */
    ImageView iv1,iv2,iv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } else {
            isGPSEnabled = false;
            isNetworkEnabled = false;
        }

        iv1 = findViewById(R.id.imageView2);
        iv2 = findViewById(R.id.imageView3);
        iv3 = findViewById(R.id.imageView4);

        iv1.bringToFront();
        iv2.bringToFront();
        iv3.bringToFront();
        tx = findViewById(R.id.textView);
        tx.bringToFront();
        Locale systemLocale = getResources().getConfiguration().locale;
        strLanguage = systemLocale.getLanguage();
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (lm != null) {
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                        100, // 통지사이의 최소 시간간격 (miliSecond)
                        1, // 통지사이의 최소 변경거리 (m)
                        mLocationListener);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                        100, // 통지사이의 최소 시간간격 (miliSecond)
                        1, // 통지사이의 최소 변경거리 (m)
                        mLocationListener);
            } catch (SecurityException ex) {
                ex.printStackTrace();
            }
        }


        Button help = findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("정보")
                        .setMessage(
                                "전체찾기 : 서울시에 있는 시설을 검색합니다.\n\n"
                        + "근처 찾기 : GPS 기반으로 시설을 검색합니다. (현 위치를 알 수 없는 경우 서울시청이 기준입니다.)\n\n"
                        + "길 찾기 : GPS 기반으로 근처에 있는 화장실을 찾습니다.\n\n"
                        + "장애인 화장실 : GPS 기반으로 근처에 있는 가장 가까운 장애인 화장실을 찾습니다.\n\n"
                        + "GPS를 사용하지 않는 경우, 시작지점을 설정하여, 장소를 찾을 수 있습니다.")
                        .setPositiveButton("확인", null).show();
            }
        });


        Button findnear = findViewById(R.id.Map);
        findnear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMapActivity(false, false, false);
            }
        });
        Button seuol = findViewById(R.id.seoul);
        seuol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SeoulMapActivity.class);
                intent.putExtra("myLatitude", myLatitude);
                intent.putExtra("myLongitude", myLongitude);
                intent.putExtra("isDefault",isDefault);
                startActivity(intent);
            }
        });
        Button findRoot = findViewById(R.id.findRoot);
        findRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openMapActivity(true, false, false);
            }
        });

        Button disfindRoot = findViewById(R.id.button);
        disfindRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              openMapActivity(true, false, true);
            }
        });

        geocoder = new Geocoder(this);
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(
                    myLatitude, // 위도
                    myLongitude, // 경도
                    10); // 얻어올 값의 개수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
        }
        if (list != null) {
            if (list.size() == 0) {
                tx.setText("알 수 없음");
            } else {
                addressLine = list.get(0).getAddressLine(0);
                addressLine = addressLine.substring(addressLine.indexOf(" ")+1);
                String []tmp = addressLine.split(" ");
                addressLine = "";
                for(String t : tmp){
                    addressLine += t + " ";
                    if(t.matches("(.*)동") || t.matches("(.*)가") || t.matches("(.*)로"))
                        break;
                }

                if (isGetGPS)
                    tx.setText(addressLine);
                else
                    tx.setText("알 수 없음");

            }
        }

        SharedPreferences pref = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        Boolean isFirst = pref.getBoolean("is_open", false);
        if(!isFirst) {
            new AlertDialog.Builder(this)
                    .setTitle("안내")
                    .setMessage("본 메세지는 최초 실행시 뜨는 메세지입니다\n\n"
                            + "본 앱은 서울열린데이터광장에서 제공하는 데이터를 이용하는 앱입니다.\n"
                            + "본 앱의 상세 길 찾기와 도로뷰는 카카오 지도를 사용합니다.\n"
                            + "본 앱의 주소정보는 화장실,흡연부스 데이터에 있는 지역을 바탕으로 만들어졌기에, 실제와의 지명이 다를 수 있습니다.\n"
                             )
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreference.edit();
                            editor.putBoolean("is_open", true);
                            editor.apply();
                        }
                    }).show();
        }

    }

    public void openMapActivity(boolean diractMe, boolean selectOne, boolean isDisable) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.putExtra("directMe", diractMe); // 길안내
        intent.putExtra("selectOne", selectOne); // 위치 하나 설정
        intent.putExtra("myLatitude", myLatitude);
        intent.putExtra("myLongitude", myLongitude);
        intent.putExtra("isDisable", isDisable);
        intent.putExtra("isDefault",isDefault);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                new AlertDialog.Builder(this)
                        .setTitle("종료")
                        .setMessage("종료하시겠어요?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                moveTaskToBack(true);
                                finish();
                            }
                        })
                        .setNegativeButton("아니오", null).show();
                return false;
            default:
                return false;
        }
    }

    public final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            myLatitude = location.getLatitude();
            myLongitude = location.getLongitude(); //경도
            List<Address> list = null;
            iv1.setImageResource(R.drawable.main_pin_connect);
            isDefault = true;
            try {
                list = geocoder.getFromLocation(
                        myLatitude, // 위도
                        myLongitude, // 경도
                        10); // 얻어올 값의 개수
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
            }
            if (list != null) {
                if (list.size() == 0) {
                    tx.setText("알 수 없음");
                } else {
                    isGetGPS = true;
                    isDefault = true;
                    addressLine = list.get(0).getAddressLine(0);
                    addressLine = addressLine.substring(addressLine.indexOf(" ")+1);
                    String []tmp = addressLine.split(" ");
                    addressLine = "";
                    for(String t : tmp){
                        addressLine += t + " ";
                        if(t.matches("(.*)동") || t.matches("(.*)가") || t.matches("(.*)로"))
                            break;
                    }
                    tx.setText(addressLine);
                }
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
