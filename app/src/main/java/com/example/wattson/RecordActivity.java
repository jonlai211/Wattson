package com.example.wattson;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class RecordActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private TextTranscriptionManager textTranscriptionManager;
    private AudioRecording currentRecording;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean isRecordingStarted = false;
    private Handler handler = new Handler();
    private Runnable updateRecordingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecordingStarted && currentRecording != null && currentRecording.isRecording()) {
                long elapsedMillis = System.currentTimeMillis() - audioManager.getRecordingStartTime() - audioManager.getPausedDuration();
                updateRecordingTime(elapsedMillis);
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        FrameLayout recordPart = findViewById(R.id.record_part);
        recordPart.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onRecordButtonLongClick(v);
            }
        });

        audioManager = new AudioManager(this);
        textTranscriptionManager = new TextTranscriptionManager();
    }

    public void onRecordButtonClick(View view) {
        TextView recordStatusText = findViewById(R.id.record_status_text);

        if (!isRecordingStarted) {
            currentRecording = audioManager.startRecording();
            isRecordingStarted = true;
            recordStatusText.setText(R.string.tap_to_pause);
            handler.post(updateRecordingTimeRunnable);
        } else if (currentRecording != null && currentRecording.isRecording()) {
            audioManager.pauseRecording(currentRecording);
            currentRecording.setRecording(false);
            recordStatusText.setText(R.string.tap_to_resume_or_long_press_to_finish);
            handler.removeCallbacks(updateRecordingTimeRunnable);
        } else {
            audioManager.resumeRecording(currentRecording);
            currentRecording.setRecording(true);
            recordStatusText.setText(R.string.tap_to_pause);
            handler.post(updateRecordingTimeRunnable);
        }
        // 更新UI
    }

    public boolean onRecordButtonLongClick(View view) {
        if (isRecordingStarted && currentRecording != null && !currentRecording.isRecording()) {
            audioManager.stopRecording(currentRecording);
            currentRecording.setRecording(false);
            isRecordingStarted = false;

            TextView recordStatusText = findViewById(R.id.record_status_text);
            recordStatusText.setText(R.string.test_show_finish);
            handler.removeCallbacks(updateRecordingTimeRunnable);
            Log.d("RecordActivity", "Long click on record button");
            return true;
        }
        return false;
    }

    public void onTranscribeButtonClick(View view) {
        Transcription transcription = textTranscriptionManager.transcribeAudio(currentRecording);
        // 显示转录文本
    }

    // Other UI related methods
    private void updateRecordingTime(long millis) {
        TextView recordingTimeView = findViewById(R.id.recording_time_text);
        String time = formatTime(millis);
        recordingTimeView.setText(time);
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / (1000 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRecordingTimeRunnable);
    }

}

