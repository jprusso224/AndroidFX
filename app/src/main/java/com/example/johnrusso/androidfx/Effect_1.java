
package com.example.johnrusso.androidfx;

import android.media.audiofx.Equalizer;
import android.support.v7.app.ActionBarActivity;
import android.os.*;
import android.view.Menu;
import android.view.*;
import android.util.Log;
import android.media.*;
import java.io.*;
import java.lang.*;

/**
 * Activity for the distortion effect
 * <p>
 * This activity records a buffer of audio from the microphone, applies the signal processing
 * to distort the audio and then plays the audio back through the speakers. The record and play back
 * functionality was modified from http://eurodev.blogspot.com/2009/09/raw-audio-manipulation-in-android.html
 *
 */
public class Effect_1 extends ActionBarActivity {

    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effect_1);

    }

    /**
     * Records 16-bit PCM audio at a frequency of 11025Hz to a file on the Android external storage device.
     * This record method uses the minimum buffer size.
     */
    public void record() {
        int frequency = 11025;
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/distort.pcm");

        isRecording = true;

        // Delete any previous recording.
        if (file.exists())
            file.delete();


        // Create the new file.
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create " + file.toString());
        }

        try {
            // Create a DataOutputStream to write the audio data into the saved file.
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);

            // Create a new AudioRecord object to record the audio.
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, channelConfiguration,
                    audioEncoding, bufferSize);

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();

            //Record for 1/4 buffer size
            long tempTime = bufferSize/4;
            while (tempTime > 0) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++)
                    dos.writeShort(buffer[i]);
                tempTime--;
            }

            audioRecord.stop();
            dos.close();

        } catch (Throwable t) {
            Log.e("AudioRecord","Recording Failed");
        }
    }

    /**
     * This function applies a distortion algorithm to the recorded audio and then plays it through
     * the Android speakers
     */
    public void play() {
        // Get the file we want to playback.
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm");
        // Get the length of the audio stored in the file (16 bit so 2 bytes per short)
        // and create a short array to store the recorded audio.
        int musicLength = (int)(file.length()/2);
        short[] music = new short[musicLength];


        try {
            // Create a DataInputStream to read the audio data back from the saved file.
            InputStream is = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            DataInputStream dis = new DataInputStream(bis);

            // Read the file into the music array.
            int i = 0;
            while (dis.available() > 0) {
              //  music[musicLength-1-i] = dis.readShort();
                music[i] = dis.readShort();

                //Clip audio
                if (music[i] > 1000) music[i] = 1000;
                if (music[i] < -1000) music[i] = -1000;

              //  Log.d("ADebugTag", "Value: " + Short.toString(music[i]));
                i++;
            }


            // Close the input streams.
            dis.close();


            // Create a new AudioTrack object using the same parameters as the AudioRecord
            // object used to create the file.
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    11025,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    musicLength,
                    AudioTrack.MODE_STREAM);

            //This equalizer should filter out the high frequencies caused from clipping
            Equalizer lowPassFilter = new Equalizer(0,audioTrack.getAudioSessionId());
            lowPassFilter.setEnabled(true);
            short numBands = lowPassFilter.getNumberOfBands();
            short startFilter = 2/4;
            short j = 0;
            for(j = (short)(startFilter*numBands); j < numBands; j++) {
                lowPassFilter.setBandLevel(j,(short)-50000);
            }
            // Start playback
            audioTrack.play();

            // Write the music buffer to the AudioTrack object
            audioTrack.write(music, 0, musicLength);


        } catch (Throwable t) {
            Log.e("AudioTrack","Playback Failed");
        }
    }

    /**
     * Called when the start button is clicked, this method calls the record method
     */
    public void startRecording(){
        Thread thread = new Thread(new Runnable() {
            public void run() {
                isRecording = true;
                record();
            }
        });
        thread.start();
        isRecording = false;

        try {
            thread.join();
        } catch (InterruptedException e) {}
    }

    /**
     * Called when the Play button is clicked, this method calls the play method.
     * Can only call play is recording is finished.
     */
    public void playDistortion(){
        if(!isRecording) {
            play();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_effect_1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
