package pntanasis.android.metronome;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class MetronomeActivity extends Activity {
	
	private final short minBpm = 40;
	private final short maxBpm = 208;
	
	private short bpm = 100;
	private short noteValue = 4;
	private short beats = 4;
	private short volume;
	private short initialVolume;
	private double beatSound = 2440;
	private double sound = 6440;
	private AudioManager audio;
    private MetronomeAsyncTask metroTask;
    
    private Button plusButton;
    private Button minusButton;
    private TextView currentBeat;
    
    private Handler mHandler;
    
    // have in mind that: http://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
    // in this case we should be fine as no delayed messages are queued
    private Handler getHandler() {
    	return new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	String message = (String)msg.obj;
            	if(message.equals("1"))
            		currentBeat.setTextColor(Color.GREEN);
            	else
            		currentBeat.setTextColor(getResources().getColor(R.color.yellow));
            	currentBeat.setText(message);
            }
        };
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        metroTask = new MetronomeAsyncTask();
        /* Set values and listeners to buttons and stuff */
        
        TextView bpmText = (TextView) findViewById(R.id.bps);
        bpmText.setText(""+bpm);
        
        TextView timeSignatureText = (TextView) findViewById(R.id.timesignature);
        timeSignatureText.setText(""+beats+"/"+noteValue);
        
        plusButton = (Button) findViewById(R.id.plus);
        plusButton.setOnLongClickListener(plusListener);
        
        minusButton = (Button) findViewById(R.id.minus);
        minusButton.setOnLongClickListener(minusListener);
        
        currentBeat = (TextView) findViewById(R.id.currentBeat);
        currentBeat.setTextColor(Color.GREEN);
        
        Spinner beatSpinner = (Spinner) findViewById(R.id.beatspinner);
        ArrayAdapter<Beats> arrayBeats =
        new ArrayAdapter<Beats>(this,
      	      android.R.layout.simple_spinner_item, Beats.values());
        beatSpinner.setAdapter(arrayBeats);
        beatSpinner.setSelection(Beats.four.ordinal());
        arrayBeats.setDropDownViewResource(R.layout.spinner_dropdown);
        beatSpinner.setOnItemSelectedListener(beatsSpinnerListener);
        
        Spinner noteValuesdSpinner = (Spinner) findViewById(R.id.notespinner);
        ArrayAdapter<NoteValues> noteValues =
        new ArrayAdapter<NoteValues>(this,
      	      android.R.layout.simple_spinner_item, NoteValues.values());
        noteValuesdSpinner.setAdapter(noteValues);
        noteValues.setDropDownViewResource(R.layout.spinner_dropdown);
        noteValuesdSpinner.setOnItemSelectedListener(noteValueSpinnerListener);
        
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
    	initialVolume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume = initialVolume;
        
        SeekBar volumebar = (SeekBar) findViewById(R.id.volumebar);
        volumebar.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumebar.setProgress(volume);
        volumebar.setOnSeekBarChangeListener(volumeListener);
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public synchronized void onStartStopClick(View view) {
    	Button button = (Button) view;
    	String buttonText = button.getText().toString();
    	if(buttonText.equalsIgnoreCase("start")) {
    		button.setText(R.string.stop);
    		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
    			metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
    		else
    			metroTask.execute();    		
    	} else {
    		button.setText(R.string.start);    	
    		metroTask.stop();
    		metroTask = new MetronomeAsyncTask();
    		Runtime.getRuntime().gc();
    	}
    }
    
    private void maxBpmGuard() {
        if(bpm >= maxBpm) {
        	plusButton.setEnabled(false);
        	plusButton.setPressed(false);
        } else if(!minusButton.isEnabled() && bpm>minBpm) {
        	minusButton.setEnabled(true);
        }    	
    }
    
    public void onPlusClick(View view) {
    	bpm++;
    	TextView bpmText = (TextView) findViewById(R.id.bps);
        bpmText.setText(""+bpm);
        metroTask.setBpm(bpm);
        maxBpmGuard();
    }
    
    private OnLongClickListener plusListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			bpm+=20;
			if(bpm >= maxBpm)
				bpm = maxBpm;
	    	TextView bpmText = (TextView) findViewById(R.id.bps);
	        bpmText.setText(""+bpm);
	        metroTask.setBpm(bpm);
	        maxBpmGuard();
			return true;
		}
    	
    };
    
    private void minBpmGuard() {
        if(bpm <= minBpm) {
        	minusButton.setEnabled(false);
        	minusButton.setPressed(false);
        } else if(!plusButton.isEnabled() && bpm<maxBpm) {
        	plusButton.setEnabled(true);
        }    	
    }
    
    public void onMinusClick(View view) {
    	bpm--;
    	TextView bpmText = (TextView) findViewById(R.id.bps);
        bpmText.setText(""+bpm);
        metroTask.setBpm(bpm);
        minBpmGuard();
    }
    
    private OnLongClickListener minusListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			bpm-=20;
			if(bpm <= minBpm)
				bpm = minBpm;
	    	TextView bpmText = (TextView) findViewById(R.id.bps);
	        bpmText.setText(""+bpm);
	        metroTask.setBpm(bpm);
	        minBpmGuard();
			return true;
		}
    	
    };
    
    private OnSeekBarChangeListener volumeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			volume = (short) progress;
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}   	
    	
    };
    
    private OnItemSelectedListener beatsSpinnerListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Beats beat = (Beats) arg0.getItemAtPosition(arg2);
			TextView timeSignature = (TextView) findViewById(R.id.timesignature);
			timeSignature.setText(""+beat+"/"+noteValue);
			metroTask.setBeat(beat.getNum());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    private OnItemSelectedListener noteValueSpinnerListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			NoteValues noteValue = (NoteValues) arg0.getItemAtPosition(arg2);
			TextView timeSignature = (TextView) findViewById(R.id.timesignature);
			timeSignature.setText(""+beats+"/"+noteValue);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    };

    @Override
    public boolean onKeyUp(int keycode, KeyEvent e) {
    	SeekBar volumebar = (SeekBar) findViewById(R.id.volumebar);
    	volume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        switch(keycode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN: 
                volumebar.setProgress(volume);
            	break;                
        }

        return super.onKeyUp(keycode, e);
    }
    
    public void onBackPressed() {
    	metroTask.stop();
//    	metroTask = new MetronomeAsyncTask();
    	Runtime.getRuntime().gc();
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    	finish();    
    }
    
    private class MetronomeAsyncTask extends AsyncTask<Void,Void,String> {
    	Metronome metronome;
    	
    	MetronomeAsyncTask() {
            mHandler = getHandler();
    		metronome = new Metronome(mHandler);
    	}

		protected String doInBackground(Void... params) {
			metronome.setBeat(beats);
			metronome.setNoteValue(noteValue);
			metronome.setBpm(bpm);
			metronome.setBeatSound(beatSound);
			metronome.setSound(sound);

			metronome.play();
			
			return null;			
		}
		
		public void stop() {
			metronome.stop();
			metronome = null;
		}
		
		public void setBpm(short bpm) {
			metronome.setBpm(bpm);
			metronome.calcSilence();
		}
		
		public void setBeat(short beat) {
			if(metronome != null)
				metronome.setBeat(beat);
		}
    	
    }

}