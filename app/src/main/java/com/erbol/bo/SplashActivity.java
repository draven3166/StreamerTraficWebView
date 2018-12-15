package com.erbol.bo;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TableLayout;
import android.widget.VideoView;

import com.erbol.bo.Utils.ConstantsUtil;

public class SplashActivity extends Activity {
    private TableLayout llIntro;
    private TableLayout layout;
    private VideoView vvIntro;
    private Button btnRadio, btnNews, btnMeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        llIntro=(TableLayout)findViewById(R.id.tl_intro);
        vvIntro=(VideoView)findViewById(R.id.vv_intro);
        layout = (TableLayout)findViewById(R.id.tl_content);
        btnRadio = (Button) findViewById(R.id.btn_cradio);
        btnNews = (Button) findViewById(R.id.btn_cnews);
        btnMeter = (Button) findViewById(R.id.btn_cmeter);
        btnRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentRadio = new Intent(SplashActivity.this, ListActivity.class);
                startActivity(intentRadio);
            }
        });
        btnNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNews = new Intent(SplashActivity.this, NewsActivity.class);
                intentNews.putExtra(ConstantsUtil.WEBURL, ConstantsUtil.URL_ROOTE);
                startActivity(intentNews);
            }
        });
        btnMeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentConfli = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(intentConfli);
            }
        });
        try {
            Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.intro);
            vvIntro.setVideoURI(video);
            vvIntro.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    llIntro.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                    aminRotation(layout);
                }
            });
            vvIntro.start();
        } catch(Exception ex) {
            llIntro.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);
            aminRotation(layout);
        }
        startService(new Intent(SplashActivity.this, ConflictService.class));
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().setFormat(1);
    }

    public void aminRotation(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animation.reset();
        animation.setDuration(500);
        view.startAnimation(animation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(SplashActivity.this, ConflictService.class));
    }
}