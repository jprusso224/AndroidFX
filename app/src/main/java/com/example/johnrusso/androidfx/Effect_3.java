
package com.example.johnrusso.androidfx;

import android.support.v7.app.ActionBarActivity;
import android.os.*;
import android.view.Menu;
import android.view.*;
import android.util.Log;
import android.media.*;
import java.io.*;
import java.lang.*;


/**
 * Activity for the flanger effect
 * <p>
 * This activity records a buffer of audio from the microphone, applies the signal processing
 * to apply flanging to the audio and then plays the audio back through the speakers. The record and play back
 * functionality was modified from http://eurodev.blogspot.com/2009/09/raw-audio-manipulation-in-android.html
 *
 */
public class Effect_3 extends ActionBarActivity {

    private boolean isRecording = false;
    private int frequency = 11025;
    private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    Thread thread = new Thread(new Runnable() {
        public void run() {
            isRecording = true;
            record(frequency,channelConfiguration,audioEncoding);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effect_1);

        thread.start();
        isRecording = false;

        try {
            thread.join();
        } catch (InterruptedException e) {}

        play();
    }

    /**
     * Records 16-bit PCM audio at a given sample frequency to a file on the Android external storage device.
     * This record method uses the minimum buffer size.
     *
     * @param frequency Frequency to sample audio
     * @param channelConfig Audio channel configuration
     * @param audioEncoding Audio encoding type (wav,pcm,etc...)
     */
    public void record(int frequency, int channelConfig, int audioEncoding) {

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/flange.pcm");

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
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfig, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, channelConfig,
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
     * This function calls the flange function and then plays the processed audio back to the user.
     */
    public void play() {
        // Get the file we want to playback.
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/flange.pcm");
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
                music[i] = dis.readShort();
                i++;
            }

            // Apply flanger to audio
            double delayRate = 1;
            double delayTime =0.003; //ms
            short[] processedAudio = applyFlanger(delayTime,delayRate,music,musicLength);

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

            // Start playback
            audioTrack.play();

            // Write the music buffer to the AudioTrack object
            audioTrack.write(processedAudio, 0, musicLength);

        } catch (Throwable t) {
            Log.e("AudioTrack","Playback Failed");
        }
        // delete file when finished
        file.delete();
    }

    /**
     * This method applies a flanging algorithm to an array of audio.
     *
     * @param delayTime Delay time for the flanger in ms
     * @param flangeRate Frequency of oscillations of the sine wave
     * @param music Audio array to be processed
     * @param musicLength Length of audio array to be processed
     * @return Returns the processed audio array
     */
    public short[] applyFlanger(double delayTime,double flangeRate, short music[] ,int musicLength){

        int maxSampleDelay = (int)(frequency*delayTime);
        double amplitude = 0.7;
        double[] sinReference = new double[musicLength];
        short[] processedMusic = new short[musicLength];

        try {
            try {
                // Create a sine reference array to create an oscillating delay
                for (int i = 0; i <= musicLength; i++) {
                    sinReference[i] = Math.sin(2 * Math.PI * i * flangeRate /(double)frequency);
                }
            }catch(Throwable throwable){
                Log.e("Flanger","Sine Reference Failed");
            }

            // Don't want to use negative samples
            for (int i = 0; i <= maxSampleDelay; i++) {
                processedMusic[i] = music[i];
            }

            try {
                // Apply flanging using the reference sine array
                double sinCurrent;
                int currentDelay;
                for (int i = maxSampleDelay; i <= musicLength; i++) {
                    sinCurrent = Math.abs(sinReference[i]);
                    currentDelay = (int) Math.ceil(sinCurrent * maxSampleDelay);
                    processedMusic[i] = (short) (amplitude * (music[i] + music[i - currentDelay]));
                }
            }catch(Throwable throwable){
                Log.e("Flanger","Algorithm Failed");
            }
        }catch(Throwable throwable){
            Log.e("Flanger","Flanger Failed");
        }

        return processedMusic;

    }
    /**
     * Called when the start button is clicked, this method calls the record method
     * <p>
     * Currently unable to have buttons start effects
     */
    public void startRecording(){

    }

    /**
     * Called when the Play button is clicked, this method calls the play method.
     * Can only call play is recording is finished.
     */
    public void playDistortion(){
        if(!isRecording) {
            //play();
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
