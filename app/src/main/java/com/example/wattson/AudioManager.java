package com.example.wattson;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioManager {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String currentRecordingPath;
    private Context context;
    private long recordingStartTime, recordingEndTime;

    public AudioManager(Context context) {
        this.context = context;
    }

    public AudioRecording startRecording() {
        Log.d("AudioManager", "Begin recording");
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        File directory = context.getExternalFilesDir(null);
        if (directory == null) {
            // Deal with error like UIHelper show toast
            return null;
        }

        String directoryPath = directory.getAbsolutePath();
        String fileName = "Recording_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".3gp";
        currentRecordingPath = directoryPath + File.separator + fileName;

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(currentRecordingPath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.i("AudioManager", "Start recording successfully");
        } catch (IOException e) {
            Log.e("AudioManager", "Start recording failed", e);
        }

        recordingStartTime = System.currentTimeMillis();

        return new AudioRecording(fileName, currentRecordingPath, 0, true);
    }

    public void stopRecording(AudioRecording recording) {
        long recordingEndTime = System.currentTimeMillis();
        long duration = recordingEndTime - recordingStartTime;

        if (mediaRecorder != null && duration > 1000) {
            try {
                mediaRecorder.stop();
                Log.d("AudioManager", "Stop recording successfully");
            } catch (IllegalStateException e) {
                Log.e("AudioManager", "Stop recording failed", e);
            } finally {
                mediaRecorder.release();
                mediaRecorder = null;
            }

            if (recording != null) {
                recording.setDuration(duration);
                recording.setRecording(false);
            }
        }
    }

    public void pauseRecording(AudioRecording recording) {
        if (mediaRecorder != null) {
            mediaRecorder.pause();
            recording.setRecording(false);
            Log.d("AudioManager", "Pause recording successfully");
        }
    }

    public void resumeRecording(AudioRecording recording) {
        if (mediaRecorder != null) {
            mediaRecorder.resume();
            recording.setRecording(true);
            Log.d("AudioManager", "Resume recording successfully");
        }
    }

    public void playRecording(String filePath) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public boolean deleteRecording(String filePath) {
        return new File(filePath).delete(); // returns true if deleted successfully
    }

}

