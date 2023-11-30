package com.example.wattson;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.SeekBar;

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
    private long recordingStartTime;
    private long pausedDuration = 0;
    private long lastPauseTime = 0;
    private boolean isPaused = false;
    private PlaybackCompletionListener playbackCompletionListener;

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
        pausedDuration = 0;

        return new AudioRecording(fileName, currentRecordingPath, 0, true);
    }

    public void stopRecording(AudioRecording recording) {
        long recordingEndTime = System.currentTimeMillis();
        long duration = recordingEndTime - recordingStartTime - pausedDuration;

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

            lastPauseTime = System.currentTimeMillis();
        }
    }

    public void resumeRecording(AudioRecording recording) {
        if (mediaRecorder != null) {
            mediaRecorder.resume();
            recording.setRecording(true);
            Log.d("AudioManager", "Resume recording successfully");

            pausedDuration += System.currentTimeMillis() - lastPauseTime;
        }
    }

    public interface PlaybackCompletionListener {
        void onPlaybackComplete();
    }

    public void setPlaybackCompletionListener(PlaybackCompletionListener listener) {
        this.playbackCompletionListener = listener;
    }

    public void playRecording(String filePath) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // handle playback completion
                        cleanupMediaPlayer();
                    }
                });
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        SeekBar seekBar = ((Activity) context).findViewById(R.id.seek_bar);
                        seekBar.setMax(mediaPlayer.getDuration());
                        mediaPlayer.start();
                    }
                });
            }

            if (isPaused) {
                resumePlayback(); // start from where it was paused
            } else {
                mediaPlayer.start(); // start from beginning
                isPaused = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            cleanupMediaPlayer();
        }
    }

    public void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    public void resumePlayback() {
        if (mediaPlayer != null && isPaused) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    private void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPaused = false;
        if (playbackCompletionListener != null) {
            playbackCompletionListener.onPlaybackComplete();
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
        stopPlayback();
        return new File(filePath).delete(); // returns true if deleted successfully
    }

    public long getRecordingStartTime() {
        return recordingStartTime;
    }

    public long getPausedDuration() {
        return pausedDuration;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
            if (isPaused) {
                onSeekWhilePaused(progress);
            }
        }
    }

    private void onSeekWhilePaused(int progress) {
        if (context instanceof RecordActivity) {
            ((RecordActivity) context).updatePlaybackTime(progress);
        }
    }

}

