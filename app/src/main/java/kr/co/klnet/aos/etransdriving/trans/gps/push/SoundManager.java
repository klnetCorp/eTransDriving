package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.SystemClock;

import java.util.HashMap;


public class SoundManager {
	
	private SoundPool mSoundPool;
	private HashMap<Integer, Integer> mSoundPoolMap;
	private AudioManager mAudioManager;
	private Context mContext;
	
	private onSoundMangerListener onSoundMangerListener = null;

	// Listener 인터페이스
	public interface onSoundMangerListener {
		public abstract void onCompleted(SoundPool soundPool, AudioManager audioManager);
	}

	// 외부에서 Listener 등록 가능하게 노출되는 Method
	public void setOnSoundMangeListener(onSoundMangerListener listener) {
		onSoundMangerListener = listener;
	}
	
	public SoundManager()
	{
		
	}
		
	public void initSounds(Context theContext) {
		 mContext = theContext;
	     //mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0); 
	     //mSoundPool = new SoundPool(1, AudioManager.STREAM_RING, 0); //벨소리
	     mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0); //미디어
	     //mSoundPool = new SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0); //전화(수신스피커)
	     
	     
	     mSoundPoolMap = new HashMap<Integer, Integer>();
	     mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
	} 
	
	public void addSound(int Index,int SoundID)
	{
		mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, Index));
		SystemClock.sleep(300); //딜레이...
	}
	
	public void playSound(int index) { 
		
		//float streamCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION); 		
		//float streamMax  = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);				
		
		//float streamVolume = streamCurrent / streamMax; 
		
	   // mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, 0, 1); 
	    
		
		//int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING); 
	    mSoundPool.play(mSoundPoolMap.get(index), 1, 1, 1, 0, 1); 
	}	
	
	public void playLoopedSound(int index) {
	     int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	     //
	     mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, -1, 1); 
	     mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener()
	     {
			
			@Override
			public void onLoadComplete(SoundPool soundPool, int arg1, int arg2)
			{
				soundPool.release();
				onSoundMangerListener.onCompleted(soundPool, mAudioManager);
			}
		});
	}
	
	
	 
	//public void VolumnUP()
	//{
		//am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//int currVol = am.getStreamVolume(StreamType);
		//int maxVol = am.getStreamMaxVolume(StreamType);
		
		//if(currVol &lt; maxVol)
		//{
		//	am.setStreamVolume(StreamType, currVol + 1, AudioManager.FLAG_PLAY_SOUND);
		//}
		
	//}
	
	//public void VolumnDOWN()
	//{
	//	am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	//	int currVol = am.getStreamVolume(StreamType);		
	//	if(currVol &gt; 0)
	//	{
	//		am.setStreamVolume(StreamType, currVol -1, AudioManager.FLAG_PLAY_SOUND);
	//	}
	//}
}