package com.erbol.bo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import com.erbol.bo.Streaming.StreamingListener;
import com.erbol.bo.Streaming.StreamingService;
import com.erbol.bo.Utils.ServiceUtil;
import com.erbol.bo.Utils.StateEthernet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayerActivityAAC extends Activity implements View.OnClickListener, StreamingListener {
	public static String StreamURL;
	private Button btnPlayStop;
	private TextView tvStatus, tvName, tvFrec;
	private SeekBar sbVolume;
	private AudioManager audioManager;
	private Handler uiHandler;
	private boolean playPause;

	private StreamingService streamingService = null;
	private ServiceConnection streamServiceConnection = null;
	private boolean streaming;
    private String streamTitle = null;
	private String frec, city, name, logo, www;
	private long idrad;
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
							Intent iweb = new Intent(PlayerActivityAAC.this, NewsActivity.class);
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
							//Toast.makeText(PlayerActivityAAC.this, "Facebook", Toast.LENGTH_SHORT).show();
							break;
					}
				}
			});
		}
		radio = new Radios();
		DBAdapter queryRad = new DBAdapter(PlayerActivityAAC.this);
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
		ImageView ivLogo = (ImageView)findViewById(R.id.iv_logo);
		Picasso.with(this).load(logo).into(ivLogo);
		btnPlayStop = (Button) findViewById(R.id.btn_playstop);
		btnPlayStop.setOnClickListener(this);
		uiHandler = new Handler();
		streamServiceConnection = new ServiceConnection(){

			@Override
			public void onServiceConnected(ComponentName cName, IBinder paramIBinder) {
				streamingService  = ((StreamingService.StreamingBinder)paramIBinder).getService();
				streamingService.setListener(PlayerActivityAAC.this);
				start();
			}

			@Override
			public void onServiceDisconnected(ComponentName cName) {
				streamingService = null;
			}
		};
		bindService(new Intent(this, StreamingService.class), streamServiceConnection, Context.BIND_AUTO_CREATE);
		Volumen();
		TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		TelephonyMgr.listen(new TeleListener(), PhoneStateListener.LISTEN_CALL_STATE);
		sendDataRadio.execute();
	}

	private void start() {
		if(streamingService.isPlayerStarted()) return;
		tvStatus.setText(getResources().getString(R.string.st_connecting));
		try {
			streamingService.startStreaming(new URI(StreamURL));
			streamingService.setVolume(0);
			tvFrec.setText(city + ", " + frec);
			btnPlayStop.setBackgroundResource(R.drawable.btn_stop);
			playPause = true;
		} catch (Exception e) {
			tvStatus.setText("Revise su conexión a Internet...");
		}
	}

	private void stop() {
		streamingService.setVolume(0);
		streamingService.stopStreaming(false);
		btnPlayStop.setBackgroundResource(R.drawable.btn_play);
		playPause = false;
	}

	@Override
	protected void onDestroy() {    
		if(streamingService != null && streaming) {
			streamingService.stopStreaming(false);
			streamingService.setVolume(0);
		}
		if(streamServiceConnection != null) {
			unbindService(streamServiceConnection);
			streamServiceConnection = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		try {
			if (!playPause) {
				start();
			} else {
				stop();
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}

    @Override
	public void streamingStarted() {
		streaming = true;		
		streamingService.setVolume(1);
        if(streamTitle == null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    //tvStatus.setText(getResources().getString(R.string.st_connecting));
                }
            });
        }
	}

    @Override
    public void buffering(int percentage) {
    }

    @Override
    public void streamingStopping() {

    }

    @Override
	public void streamingStopped() {
		streaming = false;
        streamTitle = null;
		uiHandler.post(new Runnable() {			
			@Override
			public void run() {
				tvStatus.setText(getResources().getString(R.string.txt_stop));
			}
		});
	}

	@Override
	public void streamingException(final Exception e) {
		streaming = false;
		uiHandler.post(new Runnable() {			
			@Override
			public void run() {
				tvStatus.setText(getResources().getString(R.string.st_error));
			}
		});
	}

	@Override
	public void streamingTitle(final String title) {
        streamTitle = title;
		uiHandler.post(new Runnable() {			
			@Override
			public void run() {
				tvStatus.setText(getResources().getString(R.string.st_playing));
			}
		});
	}

	private void Volumen() {
		try {
			sbVolume = (SeekBar)findViewById(R.id.sb_volume);
			audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			sbVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			sbVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

			sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void detener() {
		if(streamingService != null && streaming) {
			streamingService.stopStreaming(false);
			streamingService.setVolume(0);
		}
		if(streamServiceConnection != null) {
			unbindService(streamServiceConnection);
			streamServiceConnection = null;
		}
		finish();
	}

	public void btnClose(View v){
		detener();
	}

	@Override
	public void onBackPressed() {
		detener();
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	class TeleListener extends PhoneStateListener {
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					try {
						if (!playPause) {
							start();
							playPause = true;
						}
					} catch (Exception e) {
						e.getMessage();
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					try {
						if (playPause) {
							stop();
							playPause = false;
						}
					} catch (Exception e) {
						e.getMessage();
					}
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					try {
						if (playPause) {
							stop();
							playPause = false;
						}
					} catch (Exception e) {
						e.getMessage();
					}
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
			if (StateEthernet.verificaConexion(PlayerActivityAAC.this)) {
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