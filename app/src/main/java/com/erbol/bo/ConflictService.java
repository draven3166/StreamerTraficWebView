package com.erbol.bo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.erbol.bo.DataModel.DBAdapter;
import com.erbol.bo.Utils.ConstantsUtil;
import com.erbol.bo.Utils.StateEthernet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class ConflictService extends Service {
    public static final int NOTIFICACION_ID=1314;
    NotificationManager notificationManager;
    TimerTask timerTask;
    private int cantloc, cantidad, timecontrol;
    public ConflictService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        cantloc = 0;
        cantidad = 0;
        timecontrol = 8;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timer timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (StateEthernet.verificaConexion(ConflictService.this)) {
                    HttpURLConnection httpConnection = null;
                    BufferedReader bufferedReader = null;
                    StringBuilder response = new StringBuilder();
                    try {
                        DBAdapter dbrad = new DBAdapter(ConflictService.this);
                        dbrad.open();
                        cantloc = dbrad.cantConflicts();
                        URL url = new URL(ConstantsUtil.URL_STATECONF);
                        httpConnection = (HttpURLConnection) url.openConnection();
                        httpConnection.setRequestMethod("GET");
                        httpConnection.setRequestProperty("Content-Type", "text/plain");
                        httpConnection.connect();
                        if (httpConnection.getResponseCode()==200){
                            bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                            String line;
                            while ((line = bufferedReader.readLine()) != null){
                                response.append(line);
                            }
                            cantidad = Integer.parseInt(response.toString());
                            bufferedReader.close();
                            if (cantidad>cantloc){
                                //Intent intent= new Intent(ConflictService.this, MapsActivity.class);
                                //PendingIntent pendingIntent=PendingIntent.getActivity(ConflictService.this,0,intent,0);
                                long vibrate[] = {0,100,100};
                                NotificationCompat.Builder builder= new NotificationCompat.Builder(ConflictService.this);
                                builder.setSmallIcon(android.R.drawable.ic_dialog_info);
                                //builder.setContentIntent(pendingIntent);
                                builder.setAutoCancel(true);
                                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                                builder.setContentTitle("Nuevo Conflicto");
                                builder.setContentText("Se ha suscitado un nuevo conflicto!!!");
                                builder.setSubText("Vease la aplicación para ver el conflicto.");
                                builder.setSound(Uri.parse("android.resource://" + getApplication().getPackageName() + "/" + R.raw.notif));
                                builder.setVibrate(vibrate);
                                builder.setWhen(System.currentTimeMillis());
                                builder.setContentInfo("1");
                                notificationManager.notify(NOTIFICACION_ID, builder.build());
                                dbrad.deleteconflicts();
                                dbrad.addConflicts(cantidad);
                            }else{
                                if (cantidad<cantloc && cantloc>0) {
                                    cantloc--;
                                    dbrad.deleteconflicts();
                                    dbrad.addConflicts(cantloc);
                                }
                            }
                        }
                        dbrad.close();
                    }catch (MalformedURLException e) {
                        e.printStackTrace();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }catch (Exception e) {
                        Log.e("JSON error: ", Log.getStackTraceString(e));
                    }
                    finally {
                        if (httpConnection != null) {
                            httpConnection.disconnect();
                        }
                    }
                } else{
                    timerTask.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000*timecontrol);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerTask.cancel();
        notificationManager.cancel(NOTIFICACION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}