package com.project.mayihelpyou;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IntroActivity extends FragmentActivity {
    private String htmlContentInStringFormat = "";
    File outputFile, path;
    private TextView tx, tm, ts, tt;
    private ProgressBar progress, totalProgress;
    private AlertDialog dialog;

    String totalMessage = "(0/7)";
    String guideMessage;
    String currentMessage;
    public int progressStat = 0;
    boolean internetStat = false;
    boolean stop = false;
    boolean makeSmokeDB = false;
    boolean makeRestroomDB = false;
    boolean checkUpdateSmoke = false;
    boolean checkUpdateRestroom = false;
    boolean downloadSmoke = false;
    boolean downloadRestroom = false;
    String messageText = "";
    String partMessage = "";
    Geocoder geocoder;
    int tCount;
    String className = "IntroActivity";
    boolean isMakeAddressDB = false;
    boolean isTep = false;
    private String smokingAreaUpdate, restroomUpdate;
    private String mySmokingAreaUpdate, myRestroomUpdate;

    boolean result = false;

    // <----- 인트로 쓰래드 ----->
    class IntroThread extends Thread {
        checkUpdate update = null;
        downloadData data = null;
        checkUpdate update1 = null;
        downloadData data1 = null;

        boolean step = false;

        @Override
        public void run() {
            while (!stop) {
                /*
                if (internetStat) {
                    stop = true;
                    isConnectedInternet();
                } else {

                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm != null) {
                        NetworkInfo ni = cm.getActiveNetworkInfo();
                        internetStat = !(ni != null && ni.isConnected());
                    } else
                        internetStat = false;
                }
*/

                //    if (!internetStat) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo ni = cm.getActiveNetworkInfo();
                    if (ni != null && ni.isConnected())
                        if (ni.getType() == ConnectivityManager.TYPE_WIFI)
                            internetStat = true;
                } else
                    internetStat = false;

                if (internetStat){
                    if (!checkUpdateRestroom && !downloadRestroom && !checkUpdateSmoke && !downloadSmoke && update == null) {
                        update = new checkUpdate(defaultValue.PlaceType.RESTROOM);
                        update.execute();
                    }
                if (checkUpdateRestroom && !downloadRestroom && !checkUpdateSmoke && !downloadSmoke && data == null) {
                    data = new downloadData(defaultValue.PlaceType.RESTROOM);
                    data.execute();
                }
                if (checkUpdateRestroom && downloadRestroom && !checkUpdateSmoke && !downloadSmoke && update1 == null) {
                    update1 = new checkUpdate(defaultValue.PlaceType.SMOKING_AREA);
                    update1.execute();
                }
                if (checkUpdateRestroom && downloadRestroom && checkUpdateSmoke && !downloadSmoke && data1 == null) {
                    data1 = new downloadData(defaultValue.PlaceType.SMOKING_AREA);
                    data1.execute();
                }
            } else{
                    downloadRestroom = downloadSmoke = checkUpdateSmoke = checkUpdateRestroom = true;
                }
                    if (downloadRestroom && downloadSmoke && checkUpdateSmoke && checkUpdateRestroom && !makeSmokeDB && !step) {
                        if (update != null && !update.isCancelled()) {
                            update.cancel(true);
                        }
                        if (data != null && !data.isCancelled()) {
                            data.cancel(true);
                        }
                        if (update1 != null && !update1.isCancelled()) {
                            update1.cancel(true);
                        }
                        if (data1 != null && !data1.isCancelled()) {
                            data1.cancel(true);
                        }
                        setSmokeData();
                        step = true;
                    }
                    if (downloadRestroom && downloadSmoke && checkUpdateSmoke && checkUpdateRestroom && makeSmokeDB && step && !isMakeAddressDB) {
                        setRestRoomData();
                        step = false;
                    }

                    if (downloadRestroom && downloadSmoke && checkUpdateSmoke && checkUpdateRestroom && makeSmokeDB && !isTep && makeRestroomDB && !isMakeAddressDB) {
                        setDongData();
                        isTep = true;
                    }
                    if (downloadRestroom && downloadSmoke && checkUpdateSmoke && checkUpdateRestroom && makeSmokeDB && makeRestroomDB && isMakeAddressDB) {
                        stop = true;
                        SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreference.edit();
                        editor.putBoolean(defaultValue.IS_FIRST_UPDATE, true);
                        editor.apply();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }

           //     }
            }
        }

    }

    // <----- 권한 관련 함수 ----->
    private boolean hasPermissions(String[] permissions) {
        for (String perms : permissions) {
            if (!(checkCallingOrSelfPermission(perms) == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) return;
                    }
                } else {
                    return;
                }
                doIntro();
        }
    }

    // <----- 인터넷 연결 확인 함수 ----->
    public void isConnectedInternet() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(IntroActivity.this);
                dialog = builder.setMessage("인터넷이 연결되지 않았습니다.")
                        .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                moveTaskToBack(true);
                                finish();
                            }
                        })
                        .setOnKeyListener(new DialogInterface.OnKeyListener() {
                            public boolean onKey(DialogInterface dialog,
                                                 int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    moveTaskToBack(true);
                                    finish();
                                    return true;
                                }
                                return false;
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }

    // <----- 인트로 함수 ----->
    private void doIntro() {
        SharedPreferences pref = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        mySmokingAreaUpdate = pref.getString(defaultValue.SMOKING_AREA_DATA_UPDATE, "null");
        myRestroomUpdate = pref.getString(defaultValue.RESTROOM_DATA_UPDATE, "null");
        Boolean isFirst = pref.getBoolean(defaultValue.IS_FIRST_UPDATE, false);

        progress = findViewById(R.id.partprogressBar);
        totalProgress = findViewById(R.id.totalprogressBar);
        totalProgress.setMax(8);
        totalProgress.setProgress(0);
        progress.setMax(100);
        progress.setProgress(progressStat);
        tx = findViewById(R.id.currenttextView);
        tm = findViewById(R.id.parttextView);
       // ts = findViewById(R.id.totaltextView);
        tt = findViewById(R.id.guidetextView);
        tx.setText("로딩중...");
        tm.setText(partMessage);
        if (!isFirst) {
            tt.setText("앱 최초 실행입니다. 설정까지 2분정도 소요됩니다.");
       //     ts.setText("(0/3)");
        } else {
            tt.setText("");
       //     ts.setText(totalMessage);
        }
        if (isFirst) {
            IntroThread thread = new IntroThread();
            thread.start();
        } else {
            doFirst();
        }
    }

    public void doFirst() {
        InputStream input = null;
        OutputStream output = null;

        SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(defaultValue.RESTROOM_DATA_UPDATE, "2017.08.16");
        editor.apply();
        sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        editor = sharedPreference.edit();
        editor.putString(defaultValue.SMOKING_AREA_DATA_UPDATE, "2017.07.05");
        editor.apply();

        totalProgress.setMax(3);
        totalProgress.setProgress(0);
        progressStat = 0;
        progress.setProgress(progressStat);
        try {
            input = new BufferedInputStream(getResources().openRawResource(R.raw.restroom));
            boolean result = false;
            path = getExternalFilesDir(null);
            if (path != null)
                if (!path.exists())
                    result = path.mkdirs();
            outputFile = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE); //파일명까지 포함함 경로의 File 객체 생성
            progress.setMax(1000);
            guideMessage = "앱 최초 실행입니다. 설정까지 2분정도 소요됩니다.";
            messageText = "화장실 DB 복사중...";
        //    totalMessage = "(1/3)";
            output = new FileOutputStream(outputFile);

            runUiThread();
            byte data[] = new byte[1024];
            int count;
            int downloadedSize = 0;
            while ((count = input.read(data)) > 0) {
                progressStat = (progressStat + 1);
                progress.setProgress(progressStat);
                downloadedSize += count;
                currentMessage = String.valueOf(downloadedSize) + "KB";
                output.write(data, 0, count);
                runUiThread();
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            ExceptionLog("FIRST", "Exception", e);
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ignored) {
                ExceptionLog("FIRST", "IOException", ignored);
            }
        }

        sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        editor = sharedPreference.edit();
        editor.putInt(defaultValue.RESTROOM_DB_ROW_COUNT, 4591);
        editor.apply();

        totalProgress.setProgress(1);
        progressStat = 0;
        progress.setProgress(progressStat);
        try {
            input = new BufferedInputStream(getResources().openRawResource(R.raw.smoke));

            boolean result = false;
            path = getExternalFilesDir(null);
            if (path != null)
                if (!path.exists())
                    result = path.mkdirs();
            outputFile = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE); //파일명까지 포함함 경로의 File 객체 생성
            progress.setMax(25);
            guideMessage = "앱 최초 실행입니다. 설정까지 2분정도 소요됩니다.";
            messageText = "흡연부스 DB 복사중...";
            totalMessage = "(2/3)";
            output = new FileOutputStream(outputFile);

            runUiThread();
            byte data[] = new byte[1024];
            int count;
            int downloadedSize = 0;
            while ((count = input.read(data)) > 0) {
                progressStat = (progressStat + 1);
                progress.setProgress(progressStat);
                downloadedSize += count;
                currentMessage = String.valueOf(downloadedSize) + "KB";
                output.write(data, 0, count);
                runUiThread();
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            ExceptionLog("FIRST2", "Exception", e);
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ignored) {
                ExceptionLog("FIRST2", "IOException", ignored);
            }
        }


        sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        editor = sharedPreference.edit();
        editor.putInt(defaultValue.SMOKING_AREA_DB_ROW_COUNT, 43);
        editor.apply();


        totalProgress.setProgress(2);
        progressStat = 0;
        progress.setProgress(progressStat);
        try {
            input = new BufferedInputStream(getResources().openRawResource(R.raw.dong));

            boolean result = false;
            path = getExternalFilesDir(null);
            if (path != null)
                if (!path.exists())
                    result = path.mkdirs();
            outputFile = new File(getExternalFilesDir(null), defaultValue.DONG_DB_FILE); //파일명까지 포함함 경로의 File 객체 생성
            progress.setMax(33);
            guideMessage = "앱 최초 실행입니다. 설정까지 2분정도 소요됩니다.";
            messageText = "동 DB 복사중...";
            totalMessage = "(3/3)";
            output = new FileOutputStream(outputFile);

            runUiThread();
            byte data[] = new byte[1024];
            int count;
            int downloadedSize = 0;
            while ((count = input.read(data)) > 0) {
                progressStat = (progressStat + 1);
                progress.setProgress(progressStat);
                downloadedSize += count;
                currentMessage = String.valueOf(downloadedSize) + "KB";
                output.write(data, 0, count);
                runUiThread();
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            ExceptionLog("FIRST3", "Exception", e);
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ignored) {
                ExceptionLog("FIRST3", "IOException", ignored);
            }
        }


        sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        editor = sharedPreference.edit();
        editor.putInt(defaultValue.DONG_DB_ROW_COUNT, 464);
        editor.apply();


        totalProgress.setProgress(3);

        guideMessage = "앱 최초 실행입니다. 설정까지 2분정도 소요됩니다.";
        messageText = "마지막 설정중...";
        totalMessage = "(3/3)";
        currentMessage = "";
        runUiThread();
        /// 스텝 5 끝
        sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        editor = sharedPreference.edit();
        editor.putBoolean(defaultValue.IS_FIRST_UPDATE, true);
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    // <----- 최초 생성시 함수 ----->
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        geocoder = new Geocoder(this);
        if (!hasPermissions(defaultValue.NEED_PERMISSION)) { //퍼미션 허가를 했었는지 여부를 확인
            AlertDialog.Builder builder = new AlertDialog.Builder(IntroActivity.this);
            dialog = builder.setTitle("권한 요청").setMessage("이 앱은 다음과 같은 권한이 필요합니다.\n\n"
                    + "GPS : 현재 위치 정보 확인\n\n"
                    + "파일 쓰기/읽기 : 위치 데이터 업데이트 및 읽기")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            requestNecessaryPermissions(defaultValue.NEED_PERMISSION);//퍼미션 허가안되어 있다면 사용자에게 요청
                        }
                    })
                    .setNegativeButton("거부", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            moveTaskToBack(true);
                            finish();
                        }
                    })
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        public boolean onKey(DialogInterface dialog,
                                             int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                moveTaskToBack(true);
                                finish();
                                return true;
                            }
                            return false;
                        }
                    })
                    .create();
            dialog.show();
        } else {
            doIntro();
        }
    }

    // <----- 업데이트 체크 함수 ----->
    private class checkUpdate extends AsyncTask<Void, Void, Void> {
        defaultValue.PlaceType Type;

        checkUpdate(defaultValue.PlaceType placeType) {
            this.Type = placeType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc;
                if (Type == defaultValue.PlaceType.RESTROOM) {
                    doc = Jsoup.connect(defaultValue.GetHtmlLink(defaultValue.PlaceType.RESTROOM, defaultValue.HtmlType.WEB)).get();
                    messageText = "화장실 데이터 확인중...";
                    totalMessage = "(1/7)";
                } else {
                    doc = Jsoup.connect(defaultValue.GetHtmlLink(defaultValue.PlaceType.SMOKING_AREA, defaultValue.HtmlType.WEB)).get();
                    messageText = "흡연부스 데이터 확인중...";
                    totalMessage = "(3/7)";
                }
                publishProgress();
                Elements titles = doc.select("td div.New_Ons_DataWrap ul.Inners div.InersWrap span.DATA02");
                progressStat = 0;
                progress.setMax(titles.size());
                progress.setProgress(progressStat);
                for (Element e : titles) {
                    progressStat += 1;
                    progress.setProgress(progressStat);
                    htmlContentInStringFormat = e.text().trim();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            tx.setText(messageText);
         //   ts.setText(totalMessage);
        }

        @Override
        protected void onPostExecute(Void result) {
            File file;
            progressStat = 0;
            if (Type == defaultValue.PlaceType.RESTROOM) {
                restroomUpdate = htmlContentInStringFormat;
                file = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DATA_FILE);
            } else {
                smokingAreaUpdate = htmlContentInStringFormat;
                file = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DATA_FILE);
            }
            if (Type == defaultValue.PlaceType.RESTROOM) {
                if (myRestroomUpdate.equals(restroomUpdate)) {
                    messageText = "화장실 데이터가 최신버전 입니다.";
                    publishProgress();
                    SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreference.edit();
                    editor.putString(defaultValue.RESTROOM_DATA_UPDATE, restroomUpdate);
                    editor.apply();
                    totalProgress.setProgress(2);
                    downloadRestroom = true;
                } else {
                    if (file.exists()) {
                        messageText = "화장실 데이터가 구버전 입니다.";
                        publishProgress();
                        totalProgress.setProgress(1);
                    } else {
                        messageText = "화장실 데이터가 존재하지 않습니다.";
                        totalProgress.setProgress(1);
                        publishProgress();
                    }
                }
            } else {
                if (mySmokingAreaUpdate.equals(smokingAreaUpdate)) {
                    messageText = "흡연부스 데이터가 최신버전 입니다.";
                    publishProgress();
                    SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreference.edit();
                    editor.putString(defaultValue.SMOKING_AREA_DATA_UPDATE, smokingAreaUpdate);
                    editor.apply();
                    totalProgress.setProgress(4);
                    downloadSmoke = true;
                } else {
                    if (file.exists()) {
                        messageText = "흡연부스 데이터가 구버전 입니다.";
                        publishProgress();
                        totalProgress.setProgress(3);
                    } else {
                        messageText = "흡연부스 데이터가 존재하지 않습니다.";
                        totalProgress.setProgress(3);
                        publishProgress();
                    }
                }
            }
            if (Type == defaultValue.PlaceType.RESTROOM)
                checkUpdateRestroom = true;
            else
                checkUpdateSmoke = true;

        }

    }

    // <----- 파일 다운로드 함수 ----->
    private class downloadData extends AsyncTask<String, String, Long> {
        defaultValue.PlaceType Type;

        downloadData(defaultValue.PlaceType placeType) {
            this.Type = placeType;
        }

        @Override
        protected void onPreExecute() { //2
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(String... string_url) { //3
            int count;
            InputStream input = null;
            OutputStream output = null;
            URLConnection connection;
            long downloadedSize = 0;
            URL url;
            progressStat = 0;
            progress.setProgress(progressStat);
            try {
                if (Type == defaultValue.PlaceType.RESTROOM) {
                    url = new URL(defaultValue.GetHtmlLink(defaultValue.PlaceType.RESTROOM, defaultValue.HtmlType.DOWNLOAD));
                } else {
                    url = new URL(defaultValue.GetHtmlLink(defaultValue.PlaceType.SMOKING_AREA, defaultValue.HtmlType.DOWNLOAD));
                }
                connection = url.openConnection();
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.connect();

                input = new BufferedInputStream(url.openStream());

                boolean result = false;
                path = getExternalFilesDir(null);
                if (path != null)
                    if (!path.exists())
                        result = path.mkdirs();
                if (!result) {
                    ExceptionLog("FIRST", "Path", "");
                    return 0L;
                }

                if (Type == defaultValue.PlaceType.RESTROOM) {
                    outputFile = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DATA_FILE); //파일명까지 포함함 경로의 File 객체 생성
                    progress.setMax(7500);
                    messageText = "화장실 데이터 다운로드중...";
                    totalMessage = "(2/7)";
                } else {
                    outputFile = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DATA_FILE); //파일명까지 포함함 경로의 File 객체 생성
                    progress.setMax(12);
                    messageText = "흡연부스 데이터 다운로드중...";
                    totalMessage = "(4/7)";
                }
                output = new FileOutputStream(outputFile);


                publishProgress();
                byte data[] = new byte[1024];
                while ((count = input.read(data)) > 0) {
                    progressStat = (progressStat + 1);
                    progress.setProgress(progressStat);
                    downloadedSize += count;
                    partMessage = String.valueOf(downloadedSize) + "KB";
                    output.write(data, 0, count);
                    publishProgress();
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                ExceptionLog("DownloadSmokeFileTask-doInBackground", "Exception", e);
            } finally {
                try {
                    if (output != null) output.close();
                    if (input != null) input.close();
                } catch (IOException ignored) {
                    ExceptionLog("DownloadSmokeFileTask-doInBackground", "IOException", ignored);
                }
            }
            return downloadedSize;
        }

        @Override
        protected void onProgressUpdate(String... progressb) { //4
            super.onProgressUpdate(progressb);
            tx.setText(messageText);
            tm.setText(partMessage);
         //   ts.setText(totalMessage);

            tt.setText("다운로드 시 최대 3분 정도 소요됩니다….");
        }

        //파일 다운로드 완료 후
        @Override
        protected void onPostExecute(Long size) { //5
            super.onPostExecute(size);
            partMessage = "";
            progressStat = 100;
            progress.setMax(100);
            progress.setProgress(progressStat);
            if (Type == defaultValue.PlaceType.SMOKING_AREA) {
                // progress.setProgress(50);
                if (size > 0) {
                    //Toast.makeText(getApplicationContext(), "다운로드 완료되었습니다. 파일 크기=" + size.toString(), Toast.LENGTH_LONG).show();

                    totalProgress.setProgress(4);
                    messageText = "흡연부스 데이터가 다운로드 완료되었습니다.";
                    publishProgress();
                    SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreference.edit();
                    editor.putString(defaultValue.SMOKING_AREA_DATA_UPDATE, smokingAreaUpdate);
                    editor.apply();
                    File file = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE);
                    boolean t = file.delete();
                    if (!t)
                        ExceptionLog("DownloadSmokeFileTask-onPostExecute", "삭제실패", defaultValue.SMOKING_AREA_DATA_FILE);

                    //  tx.setText("흡연부스 데이터 다운로드 완료...");
                    downloadSmoke = true;
                } else {
                    // Toast.makeText(getApplicationContext(), "다운로드 에러", Toast.LENGTH_LONG).show();
                    ExceptionLog("DownloadSmokeFileTask-onPostExecute", "DonwloadError", defaultValue.SMOKING_AREA_DATA_FILE);
                    File file = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DATA_FILE);

                    boolean t = file.delete();
                    if (!t)
                        ExceptionLog("DownloadSmokeFileTask-onPostExecute", "삭제실패", defaultValue.SMOKING_AREA_DATA_FILE);

                }
            } else {
                // progress.setProgress(25);
                if (size > 0) {
                    // Toast.makeText(getApplicationContext(), "다운로드 완료되었습니다. 파일 크기=" + size.toString(), Toast.LENGTH_LONG).show();

                    totalProgress.setProgress(2);
                    messageText = "화장실 데이터가 다운로드 완료되었습니다.";
                    publishProgress();
                    SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreference.edit();
                    editor.putString(defaultValue.RESTROOM_DATA_UPDATE, restroomUpdate);
                    editor.apply();
                    File file = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE);

                    boolean t = file.delete();
                    if (!t)
                        ExceptionLog("DownloadSmokeFileTask-onPostExecute", "삭제실패", defaultValue.SMOKING_AREA_DATA_FILE);

                    //  tx.setText("화장실 데이터 다운로드 완료...");
                    downloadRestroom = true;
                } else {
                    // Toast.makeText(getApplicationContext(), "다운로드 에러", Toast.LENGTH_LONG).show();
                    ExceptionLog("DownloadSmokeFileTask-onPostExecute", "DonwloadError", defaultValue.RESTROOM_DATA_FILE);
                    File file = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DATA_FILE);
                    boolean t = file.delete();
                    if (!t)
                        ExceptionLog("DownloadSmokeFileTask-onPostExecute", "삭제실패", defaultValue.SMOKING_AREA_DATA_FILE);

                }
            }
        }
    }

    // <----- 뒤로가기 함수 ----->
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

    /*
     *      setSmokeData, setRestRoomData, setDongData
     *      전부 형식은 같으나 데이터 형식이 다름
     */

    public void runUiThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tt.setText(guideMessage);
                 //       ts.setText(totalMessage);
                        tx.setText(messageText);
                        tm.setText(currentMessage);
                    }
                });
            }
        }).start();
    }

    // <----- 흡연부스 데이터 베이스 생성 함수 ----->
    public void setSmokeData() {
        String json;
        int count = defaultValue.SMOKING_AREA_START_INDEX;
        progressStat = 0;
        progress.setProgress(progressStat);
        File file = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE);
        SQLiteDatabase sampleDB;
        SharedPreferences pref = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        long getRow = pref.getInt(defaultValue.SMOKING_AREA_DB_ROW_COUNT, -1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tt.setText("");
                        messageText = "흡연부스 데이터베이스 확인중...";
                        totalMessage = "(5/7)";
                   //     ts.setText(totalMessage);
                        tx.setText(messageText);
                        tm.setText("");
                    }
                });
            }
        }).start();
        if (file.exists()) {
            sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE).toString(), MODE_PRIVATE, null);
            SQLiteStatement s = sampleDB.compileStatement(String.format(" SELECT COUNT(*) FROM %s", defaultValue.DATA_DB_FIELD_NAME));
            if (s.simpleQueryForLong() == getRow) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messageText = "흡연부스 데이터베이스 확인 완료...";
                                totalMessage = "(5/7)";
                          //      ts.setText(totalMessage);
                                tx.setText(messageText);
                            }
                        });
                    }
                }).start();
                totalProgress.setProgress(5);
                makeSmokeDB = true;
                return;
            }
        }
        try {
            sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE).toString(), MODE_PRIVATE, null);
            InputStream is = new FileInputStream(new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DATA_FILE));
            int size = is.available();
            byte[] buffer = new byte[size];
            if (is.read(buffer) > -999) is.close();
            json = new String(buffer, "UTF-8");

            //long start = System.currentTimeMillis();
            JSONObject jsonObject = new JSONObject(json);
            final JSONArray jsonArray = jsonObject.getJSONArray("DATA");
            //SQLiteDatabase sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE).toString(), MODE_PRIVATE, null);
            Cursor c = sampleDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

            if (c.moveToFirst()) {
                for (; ; ) {
                    if (c.getString(0).equals(defaultValue.DATA_DB_FIELD_NAME))
                        sampleDB.execSQL(String.format("DELETE FROM %s", defaultValue.DATA_DB_FIELD_NAME));
                    if (!c.moveToNext()) break;
                }
            }
            c.close();
            sampleDB.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s ( id INTERGER , authority VARCHAR(20), area VARCHAR(20), type VARCHAR(20), operator VARCHAR(20), name_kor VARCHAR(20), add_kor VARCHAR(20), Latitude REAL, Longitude REAL, dong TEXT )", defaultValue.DATA_DB_FIELD_NAME));

            SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreference.edit();
            editor.putInt(defaultValue.SMOKING_AREA_DB_ROW_COUNT, jsonArray.length());
            editor.apply();
            for (int i = 0; i < jsonArray.length(); i++) {
                tCount = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String tts = "데이터베이스를 업데이트 중 입니다.";
                                tt.setText(tts);
                                messageText = "흡연부스 데이터베이스 생성중... ";
                                partMessage = "(" + String.valueOf(tCount + 1) + "/" + String.valueOf(jsonArray.length()) + ")";
                                tx.setText(messageText);
                                tm.setText(partMessage);
                            }
                        });
                    }
                }).start();
                progress.setMax(jsonArray.length());
                progressStat = progressStat + 1;
                progress.setProgress(progressStat);
                JSONObject order = jsonArray.getJSONObject(i);
                // -> name_kor + add_kor 가 이름
                // -> Operator -> 관리자
                // -> Type -> 타입
                // -> area -> 규모
                // -> authority -> 설치주체
                String str = "서울특별시 " + order.getString("name_kor").replace(" ", "") + " " + order.getString("add_kor");
                List<Address> list = null;
                double Latitude = 0.0f;
                double Longitude = 0.0f;
                String dong = "";
                try {
                    list = geocoder.getFromLocationName(str, 10); // 지역이름 , 읽을 개수
                } catch (IOException e) {
                    ExceptionLog("setSmokeData", "입출력 오류 - 서버에서 주소변환시 에러발생", str);
                }

                if (list != null) {
                    if (list.size() == 0) {
                        ExceptionLog("setSmokeData", "해당되는 위치정보가 없습니다.", str);
                    } else {
                        Latitude = list.get(0).getLatitude();
                        Longitude = list.get(0).getLongitude();
                        String dongs = list.get(0).getAddressLine(0);
                        dongs = dongs.replace("  ", " ");
                        String[] dongl = dongs.split(" ");
                        for (String temp : dongl)
                            if (temp.matches("(.*)동") || temp.matches("(.*)가") || temp.matches("(.*)로")) {
                                dong = temp;
                                break;
                            }
                        // 동 가 로
                    }

                }
                dong = dong.replace("'", "\\'");
                String area = order.getString("area");
                if (area.equals("null") || area.isEmpty()) area = "";
                sampleDB.execSQL("INSERT INTO " + defaultValue.DATA_DB_FIELD_NAME
                        + " Values (" + count + ",'" + order.getString("authority") + "', '"
                        + area + "', '"
                        + order.getString("cate1_name") + "', '"
                        + order.getString("operator") + "', '"
                        + order.getString("name_kor").replace(" ", "") + "', '"
                        + order.getString("add_kor") + "',"
                        + Latitude + "," + Longitude + ",'" + dong + "');");
                count++;
            }
            sampleDB.close();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tt.setText("");
                            messageText = "흡연부스 데이터베이스 생성 완료...";
                            totalMessage = "(5/7)";
                    //        ts.setText(totalMessage);
                            tx.setText(messageText);
                        }
                    });
                }
            }).start();
            totalProgress.setProgress(5);
            makeSmokeDB = true;
        } catch (SQLiteException se) {
            ExceptionLog("setSmokeData", "SQLiteException", se);
            File file1 = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DATA_FILE);
            if (file1.exists()) result = file1.delete();
            if (!result)
                ExceptionLog("setSmokeData", "삭제실패", se);
            result = file.delete();
            if (!result)
                ExceptionLog("setSmokeData", "삭제실패", se);
        } catch (JSONException e) {
            ExceptionLog("setSmokeData", "JSONException", e);
            File file1 = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DATA_FILE);
            if (file1.exists()) result = file1.delete();
            if (!result)
                ExceptionLog("setSmokeData", "삭제실패", e);
            result = file.delete();
            if (!result)
                ExceptionLog("setSmokeData", "삭제실패", e);
        } catch (IOException ie) {
            ExceptionLog("setSmokeData", "IOException", ie);
            File file1 = new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DATA_FILE);
            if (file1.exists()) result = file1.delete();
            if (!result)
                ExceptionLog("setSmokeData", "삭제실패", ie);
            result = file.delete();
            if (!result)
                ExceptionLog("setSmokeData", "삭제실패", ie);
        }
    }

    // <----- 화장실 데이터 베이스 생성 함수 ----->
    public void setRestRoomData() {
        String json;
        int count = defaultValue.RESTROOM_START_INDEX;
        int disableCount = defaultValue.RESTROOM_DISABLE_START_INDEX;
        progressStat = 0;
        progress.setProgress(progressStat);
        SQLiteDatabase sampleDB;
        SharedPreferences pref = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        File file = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE);
        long getRow = pref.getInt(defaultValue.RESTROOM_DB_ROW_COUNT, -1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tt.setText("");
                        messageText = "화장실 데이터베이스 확인중...";
                        totalMessage = "(6/7)";
               //         ts.setText(totalMessage);
                        tx.setText(messageText);
                        tm.setText("");
                    }
                });
            }
        }).start();
        if (file.exists()) {
            sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
            SQLiteStatement s = sampleDB.compileStatement(String.format(" SELECT COUNT(*) FROM %s", defaultValue.DATA_DB_FIELD_NAME));
            if (s.simpleQueryForLong() == getRow) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messageText = "화장실 데이터베이스 확인 완료...";
                                totalMessage = "(6/7)";
                    //            ts.setText(totalMessage);
                                tx.setText(messageText);
                            }
                        });
                    }
                }).start();
                totalProgress.setProgress(6);
                makeRestroomDB = true;
                return;
            }
        }

        try {
            sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
            InputStream is = new FileInputStream(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DATA_FILE));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            long start = System.currentTimeMillis();
            JSONObject jsonObject = new JSONObject(json);
            final JSONArray jsonArray = jsonObject.getJSONArray("DATA");
            //SQLiteDatabase sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
            Cursor c = sampleDB.rawQuery(String.format("SELECT name FROM sqlite_master WHERE type='%s'", "table"), null);

            if (c.moveToFirst()) {
                for (; ; ) {
                    if (c.getString(0).equals(defaultValue.DATA_DB_FIELD_NAME))
                        sampleDB.execSQL(String.format("DELETE FROM %s", defaultValue.DATA_DB_FIELD_NAME));
                    if (!c.moveToNext()) break;
                }
            }
            c.close();
            sampleDB.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s ( id INTERGER , Name VARCHAR(100), Latitude REAL, Longitude REAL, Address VARCHAR(100), Gu VARCHAR(100), RestRoomType VARCHAR(100), OpenTime VARCHAR(100), Help VARCHAR(100), MoreDiration VARCHAR(100), RestRoomStatue VARCHAR(100), DisableRestRoom VARCHAR(100), ExtraRestRoom VARCHAR(100), Dong VARCHAR(100) )", defaultValue.DATA_DB_FIELD_NAME));
            SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreference.edit();
            int test = 0;

            sampleDB.beginTransaction();
            for (int i = 0; i < jsonArray.length(); i++) {
                progress.setMax(jsonArray.length());
                progressStat = progressStat + 1;
                progress.setProgress(progressStat);
                JSONObject order = jsonArray.getJSONObject(i);
                String Name = order.getString("cot_conts_name");
                if (Name.equals("test")) {
                    test++;
                    continue;
                }
                double Latitude = order.getDouble("cot_coord_y");
                double Longitude = order.getDouble("cot_coord_x");
                String Address = order.getString("cot_addr_full_old");
                String Gu = order.getString("cot_gu_name");
                String RestRoomType = "";
                String OpenTime = "";
                String Help = "";
                String MoreDiration = "";
                String RestRoomStatue = "";
                String DisableRestRoom = "";
                String ExtraRestRoom = "";
                for (int j = 1; j < 21; j++) {
                    String TName = order.getString("cot_name_" + String.format(Locale.getDefault(), "%02d", j));
                    if (TName.isEmpty()) continue;
                    if (TName.contains("유형") || TName.contains("구분")) {
                        RestRoomType = order.getString("cot_value_" + String.format(Locale.getDefault(), "%02d", j));
                    } else if (TName.contains("위치")) {
                        MoreDiration = order.getString("cot_value_" + String.format(Locale.getDefault(), "%02d", j));
                    } else if (TName.contains("시간")) {
                        OpenTime = order.getString("cot_value_" + String.format(Locale.getDefault(), "%02d", j));
                    } else if (TName.contains("문의")) {
                        Help = order.getString("cot_name_" + String.format(Locale.getDefault(), "%02d", j)) + "\n" + order.getString("cot_value_" + String.format(Locale.getDefault(), "%02d", j));
                    } else if (TName.contains("현황")) {
                        RestRoomStatue = order.getString("cot_value_" + String.format(Locale.getDefault(), "%02d", j));
                    } else if (TName.contains("장애인")) {
                        DisableRestRoom = order.getString("cot_value_" + String.format(Locale.getDefault(), "%02d", j));
                    } else if (TName.contains("편의시설") || TName.contains("기타시설")) {
                        ExtraRestRoom = order.getString("cot_value_" + String.format(Locale.getDefault(), "%02d", j));
                    }
                }

                Name = Name.replace("'", "''");
                Address = Address.replace("'", "\\'");
                Address = Address.replace("  ", " ");
                if (Gu.isEmpty() || Gu.equals("") || Gu.equals("null")) {
                    String[] dongl = Address.split(" ");
                    for (String temp : dongl)
                        if (temp.matches("(.*)구")) {
                            Gu = temp;
                            break;
                        }
                }
                Gu = Gu.replace("'", "\\'");
                if (Address.contains(Gu))
                    Address = Address.substring(Address.indexOf(Gu) + Gu.length() + 1);
                if (Address.contains(Gu))
                    Address = Address.substring(Address.indexOf(Gu) + Gu.length() + 1);
                RestRoomType = RestRoomType.replace("'", "\\'");
                OpenTime = OpenTime.replace("'", "\\'");
                Help = Help.replace("'", "\\'");
                MoreDiration = MoreDiration.replace("'", "\\'");
                RestRoomStatue = RestRoomStatue.replace("'", "\\'");
                DisableRestRoom = DisableRestRoom.replace("'", "\\'");
                ExtraRestRoom = ExtraRestRoom.replace("'", "\\'");
                String Dong = Address;
                if (Dong.contains(" "))
                    Dong = Dong.substring(0, Dong.indexOf(" "));
                if (Dong.contains("-"))
                    Dong = Dong.substring(0, Dong.indexOf("-"));
                if (Dong.isEmpty() || Dong.equals("null"))
                    Dong = "";
                try {
                    Dong = String.valueOf(Double.parseDouble(Dong) - Double.parseDouble(Dong));
                    Dong = "";
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (Gu.equals("null") || Gu.isEmpty())
                    Gu = "";
                if (Address.equals("null") || Address.isEmpty())
                    Address = "";
                if (Help.equals("null"))
                    Help = "";
                if (OpenTime.equals("null"))
                    OpenTime = "";
                if (MoreDiration.equals("null"))
                    MoreDiration = "";
                if (RestRoomStatue.equals("null"))
                    RestRoomStatue = "";
                if (DisableRestRoom.equals("null"))
                    DisableRestRoom = "";
                if (ExtraRestRoom.equals("null") || ExtraRestRoom.equals("0"))
                    ExtraRestRoom = "";
                if (RestRoomStatue.contains("장애인")) {
                    DisableRestRoom = RestRoomStatue.substring(RestRoomStatue.indexOf("장애인"));
                    RestRoomStatue = RestRoomStatue.substring(0, RestRoomStatue.indexOf("장애인"));
                    if (DisableRestRoom.contains("구분"))
                        DisableRestRoom = "남녀구분";
                    else if (DisableRestRoom.contains("공용"))
                        DisableRestRoom = "남녀공용";
                    RestRoomStatue = RestRoomStatue.replace("(", "  ");
                    RestRoomStatue = RestRoomStatue.substring(0, RestRoomStatue.length() - 2);
                }

                ContentValues cv = new ContentValues();
                cv.put("id", (DisableRestRoom.equals("") || DisableRestRoom.isEmpty() ? count : disableCount));
                cv.put("Name", Name);
                cv.put("Latitude", Latitude);
                cv.put("Longitude", Longitude);
                cv.put("Address", Address);
                cv.put("Gu", Gu);
                cv.put("RestRoomType", RestRoomType);
                cv.put("OpenTime", OpenTime);
                cv.put("Help", Help);
                cv.put("MoreDiration", MoreDiration);
                cv.put("RestRoomStatue", RestRoomStatue);
                cv.put("DisableRestRoom", DisableRestRoom);
                cv.put("ExtraRestRoom", ExtraRestRoom);
                cv.put("Dong", Dong);

                sampleDB.insert(defaultValue.DATA_DB_FIELD_NAME, null, cv);
/*
                    sampleDB.execSQL( String.format(Locale.getDefault(),"INSERT INTO %s Values (%d, '%s',%f,%f,'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s','%s');"
                            ,defaultValue.DATA_DB_FIELD_NAME, (DisableRestRoom.equals("") || DisableRestRoom.isEmpty() ? count :disableCount), Name ,
                            Latitude ,Longitude , Address ,Gu , RestRoomType ,OpenTime , Help , MoreDiration ,RestRoomStatue , DisableRestRoom ,ExtraRestRoom , Dong ));
                            */
                if (DisableRestRoom.equals("")) {
                    count++;
                } else {
                    disableCount++;
                }
                tCount = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String ttt = "데이터베이스를 업데이트 중 입니다.";
                                tt.setText(ttt);
                                messageText = "화장실 데이터베이스 생성중...";
                                partMessage = "(" + String.valueOf(tCount + 1) + "/" + String.valueOf(jsonArray.length()) + ")";
                                tx.setText(messageText);
                                tm.setText(partMessage);
                            }
                        });
                    }
                }).start();
            }

            //   sampleDB.setTransactionSuccessful();
            //// sampleDB.endTransaction();

            sampleDB.close();
            editor.putInt(defaultValue.RESTROOM_DB_ROW_COUNT, jsonArray.length() - test);
            editor.apply();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messageText = "화장실 데이터베이스 생성 완료...";
                            totalMessage = "(6/7)";
                //            ts.setText(totalMessage);
                            tx.setText(messageText);
                        }
                    });
                }
            }).start();
            totalProgress.setProgress(6);
            makeRestroomDB = true;
            long fin = System.currentTimeMillis();
            ExceptionLog("setRestRoomData", "SQLiteException", fin - start);
        } catch (SQLiteException se) {
            ExceptionLog("setRestRoomData", "SQLiteException", se);
            File file1 = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DATA_FILE);
            if (file1.exists()) result = file1.delete();
            if (!result)
                ExceptionLog("setRestRoomData", "삭제실패", se);
            result = file.delete();
            if (!result)
                ExceptionLog("setRestRoomData", "삭제실패", se);
        } catch (JSONException e) {
            ExceptionLog("setRestRoomData", "JSONException", e);
            File file1 = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DATA_FILE);
            if (file1.exists()) result = file1.delete();
            if (!result)
                ExceptionLog("setRestRoomData", "삭제실패", e);
            result = file.delete();
            if (!result)
                ExceptionLog("setRestRoomData", "삭제실패", e);
        } catch (IOException io) {
            ExceptionLog("setRestRoomData", "IOException", io);
            File file1 = new File(getExternalFilesDir(null), defaultValue.RESTROOM_DATA_FILE);
            if (file1.exists()) result = file1.delete();
            if (!result)
                ExceptionLog("setRestRoomData", "삭제실패", io);
            result = file.delete();
            if (!result)
                ExceptionLog("setRestRoomData", "삭제실패", io);
        }
    }

    // <----- 동 데이터 베이스 생성 함수 ----->
    public void setDongData() {
        progressStat = 0;
        progress.setProgress(progressStat);
        File file = new File(getExternalFilesDir(null), defaultValue.DONG_DB_FILE);
        SQLiteDatabase sampleDB;
        SharedPreferences pref = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
        long getRow = pref.getInt(defaultValue.DONG_DB_ROW_COUNT, -1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tt.setText("");
                        messageText = "지역 데이터베이스 확인중...";
                        totalMessage = "(7/7)";
                 //       ts.setText(totalMessage);
                        tx.setText(messageText);
                        tm.setText("");
                    }
                });
            }
        }).start();
        if (file.exists()) {
            sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.DONG_DB_FILE).toString(), MODE_PRIVATE, null);
            SQLiteStatement s = sampleDB.compileStatement(" SELECT COUNT(*) FROM " + defaultValue.DATA_DB_FIELD_NAME);
            if (s.simpleQueryForLong() == getRow) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messageText = "지역 데이터베이스  확인 완료...";
                                totalMessage = "(7/7)";
                       //         ts.setText(totalMessage);
                                tx.setText(messageText);
                            }
                        });
                    }
                }).start();
                totalProgress.setProgress(8);
                isMakeAddressDB = true;
                return;
            }
        }
        try {
            sampleDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.DONG_DB_FILE).toString(), MODE_PRIVATE, null);

            SQLiteDatabase restroomDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.RESTROOM_DB_FILE).toString(), MODE_PRIVATE, null);
            SQLiteDatabase smokeDB = this.openOrCreateDatabase(new File(getExternalFilesDir(null), defaultValue.SMOKING_AREA_DB_FILE).toString(), MODE_PRIVATE, null);

            Cursor c1 = restroomDB.rawQuery("SELECT distinct gu,dong FROM " + defaultValue.DATA_DB_FIELD_NAME, null);
            Cursor c2 = smokeDB.rawQuery("SELECT distinct name_kor,dong  FROM " + defaultValue.DATA_DB_FIELD_NAME, null);

            sampleDB.execSQL("CREATE TABLE IF NOT EXISTS " + defaultValue.DATA_DB_FIELD_NAME
                    + " ( gu TEXT, " +
                    "dong TEXT )");

            SharedPreferences sharedPreference = getSharedPreferences(defaultValue.PREFERENCES_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreference.edit();
            final int totalCount = c1.getCount() + c2.getCount();
            int i;
            int k = 0;
            c1.moveToFirst();
            for (i = 0; i < c1.getCount(); c1.moveToNext(), i++) {
                tCount = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String ttt = "데이터베이스를 업데이트 중 입니다.";
                                messageText = "지역 데이터베이스 생성중... ";
                                tt.setText(ttt);
                                partMessage = "(" + String.valueOf(tCount + 1) + "/" + String.valueOf(totalCount) + ")";
                                tx.setText(messageText);
                                tm.setText(partMessage);
                            }
                        });
                    }
                }).start();
                progress.setMax(totalCount);
                progressStat = progressStat + 1;
                progress.setProgress(progressStat);
                Cursor c3 = sampleDB.rawQuery("SELECT distinct gu,dong  FROM " + defaultValue.DATA_DB_FIELD_NAME + " WHERE gu='" + c1.getString(0) + "' and dong='" + c1.getString(1) + "'", null);
                if (c3.getCount() == 0)
                    if (!c1.getString(0).isEmpty() && !c1.getString(0).equals("") && !c1.getString(1).equals("") && !c1.getString(1).isEmpty()) {
                        sampleDB.execSQL("INSERT INTO " + defaultValue.DATA_DB_FIELD_NAME
                                + " Values ('" + c1.getString(0) + "','" + c1.getString(1) + "');");
                        k++;
                    }
                c3.close();
            }
            c2.moveToFirst();
            for (; i < c1.getCount() + c2.getCount(); c2.moveToNext(), i++) {
                tCount = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String ttt = "데이터베이스를 업데이트 중 입니다.";
                                tt.setText(ttt);
                                messageText = "지역 데이터베이스 생성중... ";
                                partMessage = "(" + String.valueOf(tCount + 1) + "/" + String.valueOf(totalCount) + ")";
                                tx.setText(messageText);
                                tm.setText(partMessage);
                            }
                        });
                    }
                }).start();
                progress.setMax(totalCount);
                progressStat = progressStat + 1;
                progress.setProgress(progressStat);
                Cursor c3 = restroomDB.rawQuery(String.format("SELECT distinct gu,dong  FROM %s WHERE gu='%s' and dong='%s'", defaultValue.DATA_DB_FIELD_NAME, c2.getString(0), c2.getString(1)), null);
                if (c3.getCount() == 0)
                    if (!c2.getString(0).isEmpty() && !c2.getString(0).equals("") && !c2.getString(1).equals("") && !c2.getString(1).isEmpty()) {
                        sampleDB.execSQL(String.format("insert into %s Values ('%s','%s');", defaultValue.DATA_DB_FIELD_NAME, c2.getString(0), c2.getString(1)));

                        k++;
                    }
                c3.close();
            }

            c1.close();
            c2.close();
            sampleDB.close();


            editor.putInt(defaultValue.DONG_DB_ROW_COUNT, k);
            editor.apply();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tt.setText("");
                            messageText = "지역 데이터베이스 생성 완료...";
                            totalMessage = "(7/7)";
                  //          ts.setText(totalMessage);
                            tx.setText(messageText);
                        }
                    });
                }
            }).start();
            totalProgress.setProgress(8);
            isMakeAddressDB = true;
        } catch (SQLiteException se) {
            File file1 = new File(getExternalFilesDir(null), defaultValue.DONG_DB_FILE);
            boolean k = false;
            if (file1.exists())
                k = file1.delete();
            if (k)
                ExceptionLog("AddressDB", "SQLiteException+삭제", se);
            else
                ExceptionLog("AddressDB", "SQLiteException", se);
        }
    }

    // <----- 동 데이터 베이스 생성 함수 ----->
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
                    className,
                    MethodName,
                    Exception_type,
                    Error.toString().replace("'", "\\'")));
            tx.setText("에러가 발생하였습니다.\n다시 앱을 실행시켜주세요.");
        } catch (SQLiteException se) {
            se.printStackTrace();
        }
    }

}
