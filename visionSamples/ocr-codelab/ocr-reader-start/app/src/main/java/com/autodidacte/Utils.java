package com.autodidacte;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashMap;


class ResourceManager
{
    enum PlayerState
    {
        eNone,
        eInitialized,
        ePrepared,
        eStarted,
        ePaused,
        eFinished,
        eError
    }

    static class Audio
    {
        public MediaPlayer _mp = null;
        public PlayerState _state = PlayerState.eNone;
        public String _ressourceName = "";
        Audio(MediaPlayer mp, PlayerState state, String s)
        {
            _mp = mp;
            _state = state;
            _ressourceName = s;
        }
    }

   // private static Hashtable<Integer, Pair<MediaPlayer, PlayerState> > _sounds = new Hashtable<Integer, Pair<MediaPlayer, PlayerState> >();
    private static HashMap<Integer, Audio> _sounds = new HashMap<Integer, Audio>();
    private static HashMap<MediaPlayer, Integer> _soundIds = new HashMap<MediaPlayer, Integer>();

    static boolean mNoSound = false;

    public static MediaPlayer startSound(Context context, int soundId, boolean loop, float volumePercent, String resName)
    {
        if(mNoSound)
            volumePercent = 0.0f;
        MediaPlayer mp = null;
        if(!_sounds.containsKey(soundId)) {
            mp = MediaPlayer.create(context, soundId);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Integer soundId = _soundIds.get(mp);
                    Audio a = _sounds.get(soundId);
                    a._state = PlayerState.eStarted;
                    _sounds.put(soundId, a);
                    mp.start();
                }
            });
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    int stateId = _soundIds.get(mp);
                    Audio a = _sounds.get(stateId);
                    a._state = PlayerState.eError;
                    _sounds.put(stateId, a);
                    return false;
                }
            });
            mp.setLooping(loop);
            mp.setVolume(volumePercent, volumePercent);

            Audio a = new Audio(mp, PlayerState.eInitialized, resName);
            _sounds.put(soundId, a);
            _soundIds.put(mp, soundId);
        }
        else {
            Audio a = _sounds.get(soundId);
            String ressourceName = a._ressourceName;
            if(a != null) {
                mp = a._mp;
                try {
                    if(!mp.isPlaying())
                        mp.prepare();

                } catch (java.io.IOException ex) {

                }
            }

        }
        return mp;
    }
}

public class Utils {


    public interface IOnVideoReadyCallback {
        public void execute(VideoView video);
    }


    static VideoView mVideo;
    static int durat = 0;
    static IOnVideoReadyCallback _videoReadyCallback = null;
    // static MediaPlayer.OnPreparedListener _onPreparedListener = null;
    static MediaPlayer.OnCompletionListener _onCompletionListener;
    static int _currentVideoId = -1;

    static void setAudioVolume(int volumePercent, Activity activity) {
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max * volumePercent / 100, AudioManager.FLAG_SHOW_UI);
    }


    static void setOnVideoReadyCallback(IOnVideoReadyCallback callback) {
        _videoReadyCallback = callback;
    }
/*
    static class OnCompletionListener implements MediaPlayer.OnCompletionListener
    {
        @Override
        public void onCompletion(MediaPlayer mp)
        {
            mp.start();
        }
    }*/

    public static void setOnVideoCompletionCallback(MediaPlayer.OnCompletionListener onCompletionListener) {
        _onCompletionListener = onCompletionListener;
    }

    public static int playVideo(Activity activity, int videoId) {
        _currentVideoId = videoId;
        mVideo = (VideoView) activity.findViewById(R.id.videoView);
        if (mVideo != null) {
            Uri uri = Uri.parse("android.resource://" + activity.getPackageName() + "/" + videoId);
            mVideo.setVideoURI(uri);

            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    //int duration = mVideo.getDuration();
                    int duration = mVideo.getDuration();
                    int w = mVideo.getWidth();
                    int h = mVideo.getHeight();

                    int w2 = mp.getVideoWidth();
                    int h2 = mp.getVideoHeight();

                    mp.start();
                    if (_videoReadyCallback != null)
                        _videoReadyCallback.execute(mVideo);
                }
            });

            mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (_onCompletionListener != null)
                        _onCompletionListener.onCompletion(mp);
                    mp.start();
                }
            });

            return durat;
        }
        return 0;
    }

    public static int currentVideoId() {
        return _currentVideoId;
    }

    public static void stopVideo() {
        if (mVideo != null) {
            mVideo.stopPlayback();
        }
    }

    public static void Sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
        }
    }

    public static boolean isLetter(char c) {
        return ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'));
    }

    //static MediaPlayer _currentSound = null;
    static ArrayList<MediaPlayer> _playedSound = new ArrayList<MediaPlayer>();

    public static void playSound(Context context, int musicId, boolean loop, float volumePercent, String resName)
    {
        MediaPlayer sound = ResourceManager.startSound(context, musicId, loop, volumePercent, resName);
        _playedSound.add(sound);
    }

    public static void stopSounds()
    {
        if(_playedSound != null) {
            for (MediaPlayer m : _playedSound) {
                m.stop();
            }
            _playedSound.clear();
        }
    }
}