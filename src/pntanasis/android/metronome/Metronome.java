package pntanasis.android.metronome;

public class Metronome {
	
	private double bpm;
	private int beat;
	private int noteValue;
	private int silence;

	private double beatSound;
	private double sound;
	private final int tick = 1000; // samples of tick
	
	private boolean play = true;
	
	private AudioGenerator audioGenerator = new AudioGenerator(8000);
	
	public Metronome() {
		audioGenerator.createPlayer();
	}
	
	public void calcSilence() {
		silence = (int) (((60/bpm)*8000)-tick);		
	}
	
	public void play() {
		calcSilence();
		double[] tick = audioGenerator.getSineWave(this.tick, 8000, beatSound);
		double[] tock = audioGenerator.getSineWave(this.tick, 8000, sound);
		double silence = 0;
		double[] sound = new double[8000];
		int t = 0,s = 0,b = 0;
		do {
			for(int i=0;i<sound.length&&play;i++) {
				if(t<this.tick) {
					if(b == 0)
						sound[i] = tock[t];
					else
						sound[i] = tick[t];
					t++;
				} else {
					sound[i] = silence;
					s++;
					if(s >= this.silence) {
						t = 0;
						s = 0;
						b++;
						if(b > (this.beat-1))
							b = 0;
					}						
				}
			}
			audioGenerator.writeSound(sound);
		} while(play);
	}
	
	public void stop() {
		play = false;
		audioGenerator.destroyAudioTrack();
	}

	public double getBpm() {
		return bpm;
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
	}

	public int getNoteValue() {
		return noteValue;
	}

	public void setNoteValue(int bpmetre) {
		this.noteValue = bpmetre;
	}

	public int getBeat() {
		return beat;
	}

	public void setBeat(int beat) {
		this.beat = beat;
	}

	public double getBeatSound() {
		return beatSound;
	}

	public void setBeatSound(double sound1) {
		this.beatSound = sound1;
	}

	public double getSound() {
		return sound;
	}

	public void setSound(double sound2) {
		this.sound = sound2;
	}

}
