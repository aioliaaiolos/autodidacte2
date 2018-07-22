package com.autodidacte;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;


public class Utils {


    public interface IOnVideoReadyCallback
    {
        public void execute();
    }


    static VideoView mVideo;
    static Runnable mVideoAction = null;
    static int durat = 0;
    static IOnVideoReadyCallback _videoReadyCallback = null;


    static void setOnVideoReadyCallback(IOnVideoReadyCallback callback)
    {
        _videoReadyCallback = callback;
    }


    static class VideoAction implements Runnable {
        public void run()
        {
            mVideo.postDelayed(mVideoAction, mVideo.getDuration());
            mVideo.start();
            mVideo.seekTo(2000);
        }
    }


    public static int playVideo(Activity activity, int videoId) {
        mVideo = (VideoView) activity.findViewById(R.id.videoView);
        if (mVideo != null) {
            Uri uri = Uri.parse("android.resource://" + activity.getPackageName() + "/" + videoId);
            mVideo.setVideoURI(uri);
            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    int duration = mVideo.getDuration();
                    durat = duration;
                    mVideoAction = new VideoAction();
                    mVideo.postDelayed(mVideoAction, duration);
                    mVideo.requestFocus();
                    mVideo.start();
                    if(_videoReadyCallback != null)
                        _videoReadyCallback.execute();

                    // Recuperation d'informations
                    int pos = mVideo.getCurrentPosition();
                    float x = mVideo.getTranslationX();
                    float y = mVideo.getTranslationY();
                    float z = mVideo.getTranslationZ();
                    z = z;
                }
            });

            //int time = 0;
            //while(durat == 0 && time < 10000) {
            //    Sleep(100);
            //}
            return durat;
        }
        return 0;
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
