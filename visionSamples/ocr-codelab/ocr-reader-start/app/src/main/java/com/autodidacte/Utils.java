package com.autodidacte;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.VideoView;


public class Utils {


    public interface IOnVideoReadyCallback
    {
        public void execute(VideoView video);
    }


    static VideoView mVideo;
    static int durat = 0;
    static IOnVideoReadyCallback _videoReadyCallback = null;
   // static MediaPlayer.OnPreparedListener _onPreparedListener = null;
    static OnCompletionListener _onCompletionListener;
    static int _currentVideoId = -1;


    static void setOnVideoReadyCallback(IOnVideoReadyCallback callback)
    {
        _videoReadyCallback = callback;
    }

    static class OnCompletionListener implements MediaPlayer.OnCompletionListener
    {
        @Override
        public void onCompletion(MediaPlayer mp)
        {
            mp.start();
        }
    }


    public static int playVideo(Activity activity, int videoId) {
        _currentVideoId = videoId;
        mVideo = (VideoView) activity.findViewById(R.id.videoView);
        if (mVideo != null) {
            Uri uri = Uri.parse("android.resource://" + activity.getPackageName() + "/" + videoId);
            mVideo.setVideoURI(uri);
            /*
            if(_onPreparedListener == null)
                _onPreparedListener = new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        int duration = mVideo.getDuration();
                        int w = mVideo.getWidth();
                        int h = mVideo.getHeight();

                        //mVideo.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
                        int w2 = mp.getVideoWidth();
                        int h2 = mp.getVideoHeight();

                        durat = duration;
                        mVideo.requestFocus();
                        mVideo.start();
                        if(_videoReadyCallback != null)
                            _videoReadyCallback.execute(mVideo);
                    }
                };
                */

            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    //int duration = mVideo.getDuration();
                    int duration = mVideo.getDuration();
                    int w = mVideo.getWidth();
                    int h = mVideo.getHeight();

                    int w2 = mp.getVideoWidth();
                    int h2 = mp.getVideoHeight();

                    //durat = duration;
                    //mVideo.requestFocus();
                    //mVideo.start();
                    mp.start();
                    if(_videoReadyCallback != null)
                        _videoReadyCallback.execute(mVideo);
                }
            });

            if(_onCompletionListener == null) {
                _onCompletionListener = new OnCompletionListener();
            }
            mVideo.setOnCompletionListener(_onCompletionListener);

            return durat;
        }
        return 0;
    }

    public static int currentVideoId()
    {
        return _currentVideoId;
    }

    public static void stopVideo()
    {
        if(mVideo != null) {
            mVideo.stopPlayback();
        }
    }

    public static void Sleep(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        }
        catch(Exception e)
        {
        }
    }


}
