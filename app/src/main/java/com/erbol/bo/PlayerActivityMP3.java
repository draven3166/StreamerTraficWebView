package com.erbol.bo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.erbol.bo.DataModel.DBAdapter;
import com.erbol.bo.DataModel.Radios;
import com.erbol.bo.Utils.ConstantsUtil;
import com.erbol.bo.Utils.RayMenu;
import com.erbol.bo.Utils.StateEthernet;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlayerActivityMP3 extends Activity implements View.OnClickListener {//, View.OnKeyListener
    public static String StreamURL;
    private TextView tvStatus, tvName, tvFrec;
    private Button btnPlayStop;
    private MediaPlayer mediaPlayer;
    private boolean playPause;
    private AudioManager audioManager;
    private SeekBar sbVolume;
    private String frec, city, name, logo, www;
    private long idrad;
    private int vprogress;
    private Radios radio;
    private RayMenu rmAbout;
    private static final int[] ITEM_DRAWABLES = { R.drawable.ic_web, R.drawable.ic_send};//, R.drawable.ic_facebook
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (savedInstanceState != null) {
            idrad = savedInstanceState.getLong(ConstantsUtil.POINTRADIO);
        } else {
            idrad = getIntent().getLongExtra(ConstantsUtil.POINTRADIO, 0);
        }
        rmAbout = (RayMenu) findViewById(R.id.rm_about);
        final int itemCount = ITEM_DRAWABLES.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(ITEM_DRAWABLES[i]);
            final int position = i;
            rmAbout.addItem(item, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    switch (position){
                        case 0:
                            Intent iweb = new Intent(PlayerActivityMP3.this, NewsActivity.class);
                            iweb.putExtra(ConstantsUtil.WEBURL, www);
                            startActivity(iweb);
                            break;
                        case 1:
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "App Erbol recomendado");
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.txt_share)));
                            //Toast.makeText(PlayerActivityMP3.this, "Twitter", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            //Toast.makeText(PlayerActivityMP3.this, "Facebook", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
        radio = new Radios();
        DBAdapter queryRad = new DBAdapter(PlayerActivityMP3.this);
        queryRad.open();
        radio = queryRad.getRadio(idrad);
        queryRad.close();
        name = radio.getName();
        logo = radio.getLogo();
        StreamURL = radio.getStream();
        city = radio.getCity();
        frec = radio.getFrequency();
        www = radio.getWeb();
        tvName = (TextView) findViewById(R.id.tv_namer);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvFrec = (TextView) findViewById(R.id.tv_frec);
        tvName.setText(name);
        tvFrec.setText(city + ", " + frec);
        ImageView ivLogo = (ImageView)findViewById(R.id.iv_logo);
        Picasso.with(this).load(logo).into(ivLogo);
        btnPlayStop = (Button) findViewById(R.id.btn_playstop);
        btnPlayStop.setEnabled(false);
        btnPlayStop.setOnClickListener(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        new autoPlay().execute(StreamURL);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(tp);
        }
        Volume();
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyMgr.listen(new TeleListener(), PhoneStateListener.LISTEN_CALL_STATE);
        sendDataRadio.execute();
    }

    private void Volume() {
        try {
            sbVolume = (SeekBar)findViewById(R.id.sb_volume);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            sbVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            sbVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            //sbVolume.setOnKeyListener(this);
            sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    vprogress = progress;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        if (!playPause) {
            btnPlayStop.setBackgroundResource(R.drawable.btn_stop);
            if (!mediaPlayer.isPlaying())
                mediaPlayer.start();
            playPause = true;
            tvStatus.setText(getResources().getString(R.string.txt_play));
        } else {
            btnPlayStop.setBackgroundResource(R.drawable.btn_play);
            if (mediaPlayer.isPlaying())
                mediaPlayer.pause();
            playPause = false;
            tvStatus.setText(getResources().getString(R.string.txt_stop));
        }
    }

    class autoPlay extends AsyncTask<String, Void, Boolean> {
        // TODO Auto-generated method stub
        @Override
        protected Boolean doInBackground(String... params) {
            Boolean prepared = false;
            try {
                mediaPlayer.setDataSource(params[0]);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playPause = false;
                        btnPlayStop.setBackgroundResource(R.drawable.btn_play);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });
                mediaPlayer.prepare();
                prepared = true;
            } catch (IllegalArgumentException e) {
                //prepared = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                //prepared = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                //prepared = false;
                e.printStackTrace();
            } catch (IOException e) {
                //prepared = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result) {
                mediaPlayer.start();
                btnPlayStop.setBackgroundResource(R.drawable.btn_stop);
                playPause = true;
                if (mediaPlayer.isPlaying()) {
                    tvStatus.setText(getResources().getString(R.string.st_playing));
                    btnPlayStop.setEnabled(true);
                }
            } else {
                mediaPlayer.stop();
                btnPlayStop.setBackgroundResource(R.drawable.btn_play);
                playPause = false;
                tvStatus.setText(getResources().getString(R.string.st_error));
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvStatus.setText(getResources().getString(R.string.st_connecting));
        }
    }

    public void btnClose(View v){
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        this.finish();
    }

    class TeleListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    /*if (btnPlayStop!=null)
                        btnPlayStop.setBackgroundResource(R.drawable.btn_stop);
                    if (!mediaPlayer.isPlaying())
                        mediaPlayer.start();
                    playPause = true;
                    tvStatus.setText(getResources().getString(R.string.txt_play));*/
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (btnPlayStop!=null)
                        btnPlayStop.setBackgroundResource(R.drawable.btn_play);
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();
                    playPause = false;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (btnPlayStop!=null)
                        btnPlayStop.setBackgroundResource(R.drawable.btn_play);
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();
                    playPause = false;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    sbVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    sbVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }


    AsyncTask<Void, Void, Boolean> sendDataRadio = new AsyncTask<Void, Void, Boolean>() {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... param) {
            Boolean conexion = false;
            if (StateEthernet.verificaConexion(PlayerActivityMP3.this)) {
                HttpURLConnection httpConnection = null;
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(ConstantsUtil.URL_STATRAD+"?ident="+idrad);
                    httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setRequestMethod("GET");
                    httpConnection.setRequestProperty("Content-Type", "application/json");
                    httpConnection.connect();
                    bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                    bufferedReader.close();
                    conexion = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    conexion = false;
                } finally {
                    if (httpConnection != null) {
                        httpConnection.disconnect();
                    }
                }
            } else{
                conexion = false;
            }
            return conexion;
        }

        @Override
        protected void onPostExecute(Boolean results) {
        }
    };
}