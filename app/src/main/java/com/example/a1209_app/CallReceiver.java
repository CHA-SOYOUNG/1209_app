package com.example.a1209_app;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CallReceiver extends BroadcastReceiver {

    String phonestate;
    public GettingPHP gPHP;

    public static final String TAG_phoneState = "PHONE STATE";
    private static String url = "http://13.124.192.194:54103/user/"; // 서버 IP 주소
    public static String result = null;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")

    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        Log.d(TAG_phoneState,"onReceive()");

        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {

            TelecomManager telephonyManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

            Bundle extras = intent.getExtras();

            if (extras != null) {

                // 현재 폰 상태 가져옴
                String state = extras.getString(TelephonyManager.EXTRA_STATE);

                // 중복 호출 방지
                if (state.equals(phonestate)) {
                    return;
                } else {
                    phonestate = state;
                }

                // [벨 울리는 중]
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                    // 어플 사용 설정 ON 일 때 - 1차 판별
                    if(MainActivity.use_set==true){

                        // 수신 번호 가져옴
                        String phone_num = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        Log.d("qqq", "통화벨 울리는중");
                        Log.d("phone_number", "수신 전화번호: "+phone_num);

                        // 서버에 수신 전화번호 보내서 결과 받아옴
                        gPHP = new GettingPHP();
                        gPHP.execute(url+phone_num);

                        // 만약 신고 이력이 없다면
                        if(result != null ){
                            Toast.makeText(context, "신고 누적 :"+result, Toast.LENGTH_LONG).show();
                        }
                    }


                }
                // [통화 중]
                else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    Log.d("qqq", "통화중");

                }
                // [통화종료]
                else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    Log.d("qqq", "통화종료 혹은 통화벨 종료");
                }

            }
        }

    }

    // 서버 연동
    public class GettingPHP extends AsyncTask<String, Integer, String> { // string, int, string : 파라미터, 접속 상태, 반환 데이터 형태

        // php 에서 데이터 읽어옴
        @Override
        protected String doInBackground(String... params) { // params (파라미터) = 전화번호

            // 로그는 너무 신경쓰지 마세요 접속 잘 되나 확인하려고 넣어놓은 거예요 .. 하하
            Log.d("1conn_1", "1 ok");

            // json 타입의 데이터를 string 형태로 받아옴
            StringBuilder jsonHtml = new StringBuilder();
            try {

                // 서버 접속
                URL phpUrl = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();
                Log.d("2conn_2", "2 ok");
                Log.d("conn_state", String.valueOf(conn));


                if ( conn != null ) {

                    // 서버 접속 대기 시간이 과도하면 끊김 (우선 주석처리 해뒀어요)
                    //conn.setConnectTimeout(10000);
                    //conn.setUseCaches(false);

                    Log.d("3conn_3", "3 ok");

                    // 서버 응답 상태
                    int con_state = conn.getResponseCode();
                    Log.d("conn_r_code", String.valueOf(con_state));

                    // 서버 접속 성공했으면
                    if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                        Log.d("conn_eccc", "연결 ok");

                        //BufferReader로 결과값 읽어옴
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                        while ( true ) {
                            String line = br.readLine();
                            jsonHtml.append(line);

                            Log.d("line_value", line);
                            if ( line == null )
                                break;
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 결과값을 string 형태로 반환
            return jsonHtml.toString();
        }

        // doInBackground 에서의 return 값 활용
        @Override
        protected void onPostExecute(String str) { // str == doInBackground의 return 값
            Log.d("value_of_str", str);
            result = str;
        }
    }


}
