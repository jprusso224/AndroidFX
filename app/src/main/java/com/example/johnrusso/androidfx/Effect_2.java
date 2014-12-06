package com.example.johnrusso.androidfx;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.media.*;
import java.io.*;
import java.lang.*;

public class Effect_2 extends ActionBarActivity {

    Thread thread = null;
    AudioRecord audioRecord;
    AudioTrack audioTrack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effect_1);



        int frequency = 11025;
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        final AudioRecord audioRecord;
        final AudioTrack audioTrack;
        final int bufferSize;

        try {
            // Create a DataOutputStream to write the audio data into the saved file.

            bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, channelConfiguration,
                    audioEncoding, bufferSize);

            final short[] buffer = new short[bufferSize];

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    frequency,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM);



            thread = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            audioRecord.read(buffer, 0, bufferSize);
                            audioTrack.write(buffer, 0, buffer.length);

                        } catch (Throwable t) {
                            Log.e("Error", "Read write failed");
                            t.printStackTrace();
                        }
                    }
                }
            });


        } catch (Throwable t) {
            Log.e("AudioProcess", "Recording Failed");
        }

    }
    void startProcessing(){
        audioRecord.startRecording();
        Log.i("info", "Audio Recording started");
        audioTrack.play();
        Log.i("info", "Audio Playing started");
        thread.start();
    }

    void stopProcessing(){
        thread.stop();
        audioRecord.release();
        Log.i("info", "Audio Recording stopped");
        audioTrack.release();
        Log.i("info", "Audio Playing stopped");
    }

}
