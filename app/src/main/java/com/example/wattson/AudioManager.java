package com.example.wattson;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

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
    private boolean isRecording = false;
    private long recordingStartTime, recordingEndTime;

    public AudioManager(Context context) {
        this.context = context;
    }

    public AudioRecording startRecording() {
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
        isRecording = true;
        recordingStartTime = System.currentTimeMillis();

        return new AudioRecording(fileName, currentRecordingPath, 0);
    }

    public void stopRecording(AudioRecording recording) {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        //TODO: test "update recording duration"
        long recordingEndTime = System.currentTimeMillis();
        long duration = recordingEndTime - recordingStartTime;
        recording.setDuration(duration);
    }

    public void pauseRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.pause();
            isRecording = false;
        }
    }

    public void resumeRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.resume();
            isRecording = true;
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

