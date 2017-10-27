package com.example.igor.wifimanager2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    String networkSSID = "Home_wifi_1";
    String networkPass = "09820639";

    Button Connect_btn;
    Button Disconnect_btn;
    public TextView con_txt,txtview;

    boolean tryConnect;

    WifiManager wifiManager;
    SupplicantState connectionInfo;
    int netId;

    MyTask mt;

    List<WifiConfiguration> WifiConfig;

    private static final int NOTOFOCANION_ID =132 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Connect_btn = (Button) findViewById(R.id.connect_btn);
        con_txt = (TextView) findViewById(R.id.connectionInfo);
        txtview= (TextView) findViewById(R.id.textView2);
        Disconnect_btn = (Button) findViewById(R.id.Disconnect);

        wifiManager = (WifiManager)MainActivity.this.getSystemService(WIFI_SERVICE);

        Disconnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Disconnect();
            }
        });

        Connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectToWifi();
            }
        });

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                mt = new MyTask();
                mt.execute();
            };
        }, 0L, 2L * 1000);
    }

    public void ConnectToWifi(){
        wifiManager.removeNetwork(netId);
        tryConnect = true;
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);
        netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

    }

    public String WiFiConnectionInfo(){
        connectionInfo = wifiManager.getConnectionInfo().getSupplicantState();
        if(connectionInfo.toString() == "COMPLETED")

            showNotification(wifiManager.getConnectionInfo().getSSID());
            tryConnect=false;
        if(connectionInfo.toString() == "SCANNING" && !tryConnect)
            wifiManager.removeNetwork(netId);
        return connectionInfo.toString();
    }

    public void Disconnect(){
        wifiManager.disconnect();
        wifiManager.removeNetwork(netId);
        
    }


    public void showNotification(String title){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setTicker("WiFi Manager - подключен")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("WiFi Manager")
                .setContentText("Подключенно к: "+title)
               // .setSound(Uri.parse("android.resource://com.example.alexdev.notificationtutor/" + R.raw.calltoprayer2))
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
        ;
        android.app.Notification notification = builder.build();
        notificationManager.notify(NOTOFOCANION_ID, notification);
    }
    class MyTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... noargs) {
            return WiFiConnectionInfo();
        }

        protected void  onPostExecute(String result) {
            con_txt.setText(result);
            Toast toast = Toast.makeText(getApplicationContext(),
                    result, Toast.LENGTH_SHORT);
            toast.show();
        }


    }
}
