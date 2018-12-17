package com.project.mayihelpyou;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SeoulMapActivity extends FragmentActivity {
    double selectLat;
    double selectLon;
    double selLatitude;
    double selLongitude;
    String Type = defaultValue.DEFAULT_TYPE;
    String Gu = defaultValue.DEFAULT_GU;
    String dong = defaultValue.DEFAULT_DONG;
    String selName = "";
    boolean FindDisable = false;
    private AlertDialog dialog;
    ArrayAdapter<String> arrAdapt;
    ArrayList<String> entries;
    Geocoder geocoder;

    public String setMessage(HashMap<String, String> hashMap) {
        String Text = "";
        if (hashMap.get("what_is_it").equals("흡연부스")) {
            Text = Text + "개방 형태 : " + hashMap.get("type") + "\r\n\r\n";
            Text = Text + "크기 : " + (hashMap.get("area").isEmpty() ? "정보없음" : hashMap.get("area")) + "\r\n\r\n";
            Text = Text + "설치 주체 : " + hashMap.get("authority") + "\r\n\r\n";
            Text = Text + "관리자 : " + hashMap.get("operator") + "\r\n\r\n";
        } else {
            Text = Text + "위치 : " + hashMap.get("Gu") + " " + hashMap.get("Address") + "\r\n\r\n";
            Text = Text + (hashMap.get("MoreDiration").isEmpty() ? "" : "상세주소 : " + hashMap.get("MoreDiration") + "\r\n\r\n");
            Text = Text + (hashMap.get("type").isEmpty() ? "" : "화장실형태 : " + hashMap.get("type") + "\r\n\r\n");
            Text = Text + "개방시간 : " + (hashMap.get("OpenTime").isEmpty() ? "정보없음" : hashMap.get("OpenTime")) + "\r\n\r\n";
            Text = Text + (hashMap.get("Help").isEmpty() ? "" : hashMap.get("Help") + "\r\n\r\n");
            Text = Text + "장애인 화장실 유무 : " + (hashMap.get("DisableRestRoom").isEmpty() ? "없음" : hashMap.get("DisableRestRoom")) + "\r\n\r\n";

            if (!hashMap.get("RestRoomStatue").isEmpty()) {
                if (hashMap.get("RestRoomStatue").contains("남녀공용")) {
                    Text = Text + "화장실 정보 : 남녀공용\r\n" +
                            "                       " + hashMap.get("RestRoomStatue").substring(hashMap.get("RestRoomStatue").indexOf(":") + 1) + "\r\n";
                } else {
                    String[] tmp = hashMap.get("RestRoomStatue").split("/");
                    Text = Text + "화장실 정보 : " + tmp[0] + "\r\n" +
                            "                       " + tmp[1] + "\r\n";
                }
            }
            if (!hashMap.get("ExtraRestRoom").isEmpty()) {
                String tmp = hashMap.get("ExtraRestRoom");
                ArrayList<String> cctv = new ArrayList<>();
                ArrayList<String> child = new ArrayList<>();
                ArrayList<String> extra = new ArrayList<>();
                ArrayList<String> place = new ArrayList<>();
                //Text = Text + "설치 항목\r\n";
                if (tmp.contains("CCTV"))
                    cctv.add("CCTV");
                if (tmp.contains("교환대"))
                    child.add("기저귀교환대");
                if (tmp.contains("조기"))
                    extra.add("손건조기");
                if (tmp.contains("비상벨"))
                    cctv.add("비상벨");
                if (tmp.contains("보호의자"))
                    child.add("유아용보호의자");
                if (tmp.contains("소독기"))
                    extra.add("손소독기");
                if (tmp.contains("종이타올") || tmp.contains("페이퍼타올"))
                    extra.add("종이타올");
                if (tmp.contains("핸드타올"))
                    extra.add("핸드타올");
                if (tmp.contains("난방기"))
                    extra.add("난방기");
                if (tmp.contains("핸드드라이어"))
                    extra.add("핸드드라이어");
                if (tmp.contains("샤워시설"))
                    place.add("샤워시설");
                if (tmp.contains("편의시설"))
                    place.add("편의시설");
                if (tmp.contains("유아용소변기"))
                    child.add("유아용소변기");

                if (!cctv.isEmpty()) {
                    Text = Text + "보안 : ";
                    for (String temp : cctv) {
                        Text = Text + temp + " ";
                    }
                    Text = Text + "\r\n\r\n";
                }
                if (!child.isEmpty()) {
                    Text = Text + "유아관련 : ";
                    for (String temp : child) {
                        Text = Text + temp + " ";
                    }
                    Text = Text + "\r\n\r\n";
                }
                if (!place.isEmpty()) {
                    Text = Text + "시설 : ";
                    for (String temp : place) {
                        Text = Text + temp + " ";
                    }
                    Text = Text + "\r\n\r\n";
                }
                if (!extra.isEmpty()) {
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

    public void showMapActivity(boolean isDirect, HashMap<String, String> hashMap) {

        selLatitude = Double.valueOf(hashMap.get("Latitude"));
        selLongitude = Double.valueOf(hashMap.get("Longitude"));
        selName = hashMap.get("name");
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);

        if (hashMap.get("what_is_it").equals("화장실")) {
            intent.putExtra("Address", hashMap.get("Address"));
            intent.putExtra("Gu", hashMap.get("Gu"));
            intent.putExtra("OpenTime", hashMap.get("OpenTime"));
            intent.putExtra("Help", hashMap.get("Help"));
            intent.putExtra("MoreDiration", hashMap.get("MoreDiration"));
            intent.putExtra("RestRoomStatue", hashMap.get("RestRoomStatue"));
            intent.putExtra("DisableRestRoom", hashMap.get("DisableRestRoom"));
            intent.putExtra("ExtraRestRoom", hashMap.get("ExtraRestRoom"));
        } else {
            intent.putExtra("authority", hashMap.get("authority"));
            intent.putExtra("area", hashMap.get("area"));
            intent.putExtra("operator", hashMap.get("operator"));
            intent.putExtra("name_kor", hashMap.get("name_kor"));
            intent.putExtra("add_kor", hashMap.get("add_kor"));
        }
        intent.putExtra("name", hashMap.get("name"));
        intent.putExtra("dong", hashMap.get("dong"));
        intent.putExtra("what_is_it", hashMap.get("what_is_it"));
        intent.putExtra("id", hashMap.get("id"));
        intent.putExtra("type", hashMap.get("type"));
        intent.putExtra("Latitude", hashMap.get("Latitude"));
        intent.putExtra("Longitude", hashMap.get("Longitude"));

        intent.putExtra("directMe", isDirect); // 길안내
        intent.putExtra("selectOne", true); // 위치 하나 설정
        intent.putExtra("selLatitude", selLatitude);  // 키, 디폴트값
        intent.putExtra("selLongitude", selLongitude);  // 키, 디폴트값
        intent.putExtra("name", selName);
        intent.putExtra("myLatitude", selectLat);  // 키, 디폴트값
        intent.putExtra("myLongitude", selectLon);  // 키, 디폴트값
        intent.putExtra("isDefault",(selectLat < 3 && selectLon < 3) ? false : isDefault);
        startActivity(intent);

    }

    public final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            selectLat = location.getLatitude();
            selectLon = location.getLongitude(); //경도
            isDefault = true;
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

public void aa(){
    Thread Th = new Thread(new Runnable() {

        public void run() {
            double y = 0, x = 0;
            String result = "";
            if(!Gu.equals("전체")) {
                try {
                    URL url = new URL("https://dapi.kakao.com/v2/local/search/address.json?query='서울특별시 " + Gu + " " + (dong.equals("전체") ? "" : dong) + "'");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.addRequestProperty("Authorization", "KakaoAK 48c0f451501244282ff95cd978485f0e");

                    httpURLConnection.connect();

                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {

                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();

                    }


                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;


                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    httpURLConnection.disconnect();

                    result = sb.toString().trim();

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray placesa = jsonObject.getJSONArray("documents");

                        JSONObject place = placesa.getJSONObject(0);
                        JSONObject dd = place.getJSONObject("address");

                        x = dd.getDouble("y");
                        y = dd.getDouble("x");


                    } catch (JSONException e) {
                    }
                } catch (Exception e) {
                }
            }
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            intent.putExtra("ghkwkdtlf", Type.equals("화장실") && !FindDisable); // 위치 하나 설정
            intent.putExtra("wkddodls", Type.equals("화장실") && FindDisable);  // 키, 디폴트값
            intent.putExtra("gmqdusqntm", !Type.equals("화장실"));  // 키, 디폴트값
            intent.putExtra("dong", dong);  // 키, 디폴트값
            intent.putExtra("gu", Gu);  // 키, 디폴트값
            if(x != 0)
                intent.putExtra("myLatitude", x);  // 키, 디폴트값
            if(y != 0)
                intent.putExtra("myLongitude", y);  // 키, 디폴트값
            intent.putExtra("isMapSerach", true);
            startActivity(intent);
        }});
Th.start();
}
    public boolean a =false, b=false, c = false, isDefault = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_seoul);
        Intent intent = getIntent();  // 넘어온 Intent 객체를 받는다
        isDefault = intent.getBooleanExtra("isDefault",false);
        selectLat = intent.getDoubleExtra("myLatitude", 1);
        selectLon = intent.getDoubleExtra("myLongitude", 1);
        final ArrayList<HashMap<String, String>> items = new ArrayList<>();
        final BaseAdapter adapter = new ListViewItem(this, items) ;


        boolean isGPSEnabled,isNetworkEnabled;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } else {
            isGPSEnabled = false;
            isNetworkEnabled = false;
        }

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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



        geocoder = new Geocoder(this);
        final ListView listview = findViewById(R.id.listView);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SeoulMapActivity.this);
               // final HashMap<String, String> hashMap = (HashMap<String, String>) parent.getItemAtPosition(position);
                final HashMap<String, String> hashMap = (HashMap<String, String>) parent.getItemAtPosition(position);
                if (!hashMap.get("what_is_it").equals("정보없음")) {
                    dialog = builder.setMessage(setMessage(hashMap))
                            .setTitle(hashMap.get("what_is_it").equals("화장실") ?
                                    hashMap.get("name") :
                                    hashMap.get("name_kor") + " " + hashMap.get("dong") + " " + hashMap.get("add_kor"))
                            .setNegativeButton("길안내", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dd, int whichButton) {
                                    showMapActivity(true,hashMap);
                                }
                            })
                            .setNeutralButton("위치확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dd, int whichButton) {
                                    showMapActivity(false,hashMap);
                                }
                            })
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                }
            }
        });

        final Button seoulRestRoom = findViewById(R.id.seoulrestroom);
        final Button seoulDisable = findViewById(R.id.seouldisable);
        final Button seoulSmoke = findViewById(R.id.seoulsmoke);

        seoulRestRoom.setBackgroundResource(R.drawable.button_restroom_png);
        seoulDisable.setBackgroundResource(R.drawable.button_disable_restroom_png);
        seoulSmoke.setBackgroundResource(R.drawable.button_smoke_png);
        seoulRestRoom.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(MotionEvent.ACTION_DOWN == event.getAction() || MotionEvent.ACTION_MOVE == event.getAction()) {
                        seoulRestRoom.setBackgroundResource(R.drawable.press_button_restroom_png);
                }
                if(MotionEvent.ACTION_UP == event.getAction()){
                    if(a == false) {
                        seoulRestRoom.setBackgroundResource(R.drawable.click_button_restroom_png);
                        seoulDisable.setBackgroundResource(R.drawable.button_disable_restroom_png);
                        seoulSmoke.setBackgroundResource(R.drawable.button_smoke_png);
                        a=true;
                        b=false;
                        c=false;
                        FindDisable=false;
                        Type = "화장실";
                    } else{
                        seoulRestRoom.setBackgroundResource(R.drawable.button_restroom_png);
                        a=false;
                    }

                }
                return true;
            }});
        seoulDisable.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                    if(MotionEvent.ACTION_DOWN == event.getAction() || MotionEvent.ACTION_MOVE == event.getAction()) {
                        seoulDisable.setBackgroundResource(R.drawable.press_button_disable_restroom_png);
                    }
                    if(MotionEvent.ACTION_UP == event.getAction()){
                        if(b == false) {
                            seoulRestRoom.setBackgroundResource(R.drawable.button_restroom_png);
                            seoulDisable.setBackgroundResource(R.drawable.click_button_disable_restroom_png);
                            seoulSmoke.setBackgroundResource(R.drawable.button_smoke_png);
                            a=false;
                            b=true;
                            c=false;
                            FindDisable = true;
                            Type = "화장실";
                        } else{
                            Type = "화장실";
                            seoulDisable.setBackgroundResource(R.drawable.button_disable_restroom_png);
                            b=false;
                            FindDisable = false;
                        }

                    }
                return true;}});

        seoulSmoke.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(MotionEvent.ACTION_DOWN == event.getAction() || MotionEvent.ACTION_MOVE == event.getAction()) {
                    seoulSmoke.setBackgroundResource(R.drawable.press_button_smoke_png);
                }
                if(MotionEvent.ACTION_UP == event.getAction()){
                    if(c == false) {
                        seoulRestRoom.setBackgroundResource(R.drawable.button_restroom_png);
                        seoulDisable.setBackgroundResource(R.drawable.button_disable_restroom_png);
                        seoulSmoke.setBackgroundResource(R.drawable.click_button_smoke_png);
                        a=false;
                        b=false;
                        c=true;
                        Type = "흡연부스";
                    } else{
                        Type = "화장실";
                        seoulSmoke.setBackgroundResource(R.drawable.button_smoke_png);
                        c=false;
                    }

                }
                return true;}});
        Button listButton = findViewById(R.id.listcheck);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!a&&!b&&!c){
                    Toast.makeText(getApplicationContext(), "검색하시려는 종류를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    items.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(SeoulMapActivity.this);
                        dialog = builder.setMessage("어떤 방법으로 검색하시겠습니까?")
                                .setNegativeButton("지도(데이터 사용)", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        aa();
                                    }
                                })
                                .setPositiveButton("리스트(데이터미사용)", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            Cursor c;
                                            SQLiteDatabase sampleDB;
                                            if (Type.equals("화장실")) {
                                                String dbName = "restroom.db";
                                                sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), dbName).toString(), MODE_PRIVATE, null);

                                                if (Gu.equals("전체")) {
                                                    c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME + (FindDisable ? " WHERE (DisableRestroom is not null) and (DisableRestroom is not \"\")" : "") + " order by gu asc Limit 200", null);

                                                    Toast.makeText(getApplicationContext(), "데이터가 너무 많아 200개만 표시합니다.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (!FindDisable)
                                                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME + " WHERE gu='" + Gu + "' " + (dong.equals("전체") == false ? " and dong='" + dong + "'" : "") + " order by gu asc", null);
                                                    else
                                                        c = sampleDB.rawQuery("SELECT * FROM " + defaultValue.DATA_DB_FIELD_NAME + " WHERE (gu='" + Gu + "') and id >= " + defaultValue.RESTROOM_DISABLE_START_INDEX + (dong.equals("전체") == false ? " and dong='" + dong + "'" : "") + " order by gu asc", null);
                                                }
                                                items.clear();
                                                adapter.notifyDataSetChanged();

                                                if (c != null) {
                                                    if (c.moveToFirst()) {
                                                        for (; ; ) {
                                                            HashMap<String, String> hmitem = new HashMap<>();
                                                            hmitem.put("what_is_it", "화장실");
                                                            hmitem.put("id", c.getString(0));
                                                            hmitem.put("name", c.getString(1));
                                                            hmitem.put("Latitude", c.getString(2));
                                                            hmitem.put("Longitude", c.getString(3));
                                                            hmitem.put("Address", c.getString(4));
                                                            hmitem.put("description", c.getString(5) + " " + c.getString(4)); // -> address
                                                            hmitem.put("Gu", c.getString(5));
                                                            hmitem.put("type", c.getString(6));
                                                            hmitem.put("OpenTime", c.getString(7));
                                                            hmitem.put("Help", c.getString(8));
                                                            hmitem.put("MoreDiration", c.getString(9));
                                                            hmitem.put("RestRoomStatue", c.getString(10));
                                                            hmitem.put("DisableRestRoom", c.getString(11));
                                                            hmitem.put("ExtraRestRoom", c.getString(12));
                                                            hmitem.put("dong", c.getString(13));
                                                            items.add(hmitem);

                                                            if (!c.moveToNext()) break;
                                                        }
                                                    }
                                                }

                                            } else {
                                                sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE).toString(), MODE_PRIVATE, null);
                                                if (Gu.equals("전체")) {
                                                    c = sampleDB.rawQuery(String.format("SELECT * FROM %s order by name_kor asc", defaultValue.DATA_DB_FIELD_NAME), null);
                                                } else {
                                                    c = sampleDB.rawQuery("SELECT * FROM " +  defaultValue.DATA_DB_FIELD_NAME + " WHERE name_kor='"+Gu+"' "+(!dong.equals("전체") ? " and dong='" + dong + "'" : "")+" order by name_kor asc", null);
                                                }
                                                items.clear();
                                                adapter.notifyDataSetChanged();
                                                if (c != null) {
                                                    if (c.moveToFirst()) {
                                                        for (; ; ) {
                                                            HashMap<String, String> hmitem = new HashMap<>();
                                                            hmitem.put("what_is_it", "흡연부스");
                                                            hmitem.put("name", c.getString(5) + " " + c.getString(9) + " " + c.getString(6));
                                                            hmitem.put("description", c.getString(3));
                                                            hmitem.put("Latitude", c.getString(7));
                                                            hmitem.put("Longitude", c.getString(8));
                                                            hmitem.put("id", c.getString(0));
                                                            hmitem.put("dong", c.getString(9));
                                                            hmitem.put("authority", c.getString(1));
                                                            hmitem.put("area", c.getString(2));
                                                            hmitem.put("type", c.getString(3));
                                                            hmitem.put("operator", c.getString(4));
                                                            hmitem.put("name_kor", c.getString(5));
                                                            hmitem.put("add_kor", c.getString(6));
                                                            items.add(hmitem);
                                                            if (!c.moveToNext()) break;
                                                        }
                                                    }
                                                }
                                            }
                                            if (items.isEmpty()) {
                                                HashMap<String, String> hmitem = new HashMap<>();
                                                hmitem.put("what_is_it", "정보없음");
                                                hmitem.put("name", "정보가 없습니다.");
                                                hmitem.put("description", "정보가 없어요...");
                                                hmitem.put("id", "77777777");
                                                items.add(hmitem);
                                            }
                                            adapter.notifyDataSetChanged();
                                            c.close();
                                        } catch (SQLiteException e) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                                .create();
                        dialog.show();

            }
        });


        entries = new ArrayList<>();
        entries.add("전체");

        arrAdapt = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, entries);


        final Spinner sss = findViewById(R.id.spinner3);
        sss.setAdapter(arrAdapt);
        sss.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                dong = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner s = findViewById(R.id.spinner);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            ListPopupWindow window = (ListPopupWindow)popup.get(s);
            window.setHeight(900); //pixel
        } catch (Exception e) {
            e.printStackTrace();
        }


        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Gu = parent.getItemAtPosition(position).toString();
                checkLocation(parent.getItemAtPosition(position).toString());
                sss.setSelection(0);

                try {
                    Field popup = Spinner.class.getDeclaredField("mPopup");
                    popup.setAccessible(true);

                    ListPopupWindow window = (ListPopupWindow)popup.get(sss);
                    window.setHeight(entries.size() > 7 ? 900 : entries.size() * 135); //pixel
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }
    public void ExceptionLog(String MethodName, String Exception_type, Object Error) {
        try {
            SQLiteDatabase sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.EXCEPTION_LOG_DB).toString(), MODE_PRIVATE, null);
            sampleDB.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (Date VARCHAR(50), Class VARCHAR(50), Method VARCHAR(50), ExceptionType VARCHAR(50), Log TEXT);",
                    defaultValue.EXCEPTION_LOG_DB_FIELD_NAME));
            long mNow = System.currentTimeMillis();
            Date mDate = new Date(mNow);
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
            mFormat.format(mDate);

            sampleDB.execSQL(String.format("insert into %s values ('%s','%s','%s','%s','%s');",
                    defaultValue.EXCEPTION_LOG_DB_FIELD_NAME, mFormat.format(mDate),
                    "SeoulMapActivity",
                    MethodName,
                    Exception_type,
                    Error.toString().replace("'", "\\'")));
        } catch (SQLiteException se) {
            se.printStackTrace();
        }
    }
    public void checkLocation(String text) {
        entries.clear();
        entries.add("전체");
        SQLiteDatabase sampleDB = openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.DONG_DB_FILE).toString(), MODE_PRIVATE, null);
        Cursor c = sampleDB.rawQuery(String.format("SELECT dong  FROM %s WHERE gu='%s' order by dong asc", defaultValue.DATA_DB_FIELD_NAME, text), null);

        if (c.moveToFirst()) {
            for (; ; ) {
                entries.add(c.getString(0));
                if (!c.moveToNext()) break;
            }
        }
        c.close();
        arrAdapt.notifyDataSetChanged();
    }
}

