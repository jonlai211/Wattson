package com.example.wattson;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        TextView recordingTimeView = findViewById(R.id.recording_time_text);

        if (!isRecordingStarted) {
            currentRecording = audioManager.startRecording();
            isRecordingStarted = true;
            recordStatusText.setText(R.string.tap_to_pause);
            recordingTimeView.setVisibility(View.VISIBLE);
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
        TextView recordingTimeView = findViewById(R.id.recording_time_text);

        if (isRecordingStarted && currentRecording != null && !currentRecording.isRecording()) {
            audioManager.stopRecording(currentRecording);
            currentRecording.setRecording(false);
            isRecordingStarted = false;

            TextView recordStatusText = findViewById(R.id.record_status_text);
            recordStatusText.setText(R.string.test_show_finish);
            recordingTimeView.setVisibility(View.INVISIBLE);
            recordStatusText.setVisibility(View.INVISIBLE);

            FrameLayout recordPart = findViewById(R.id.record_part);
            recordPart.setClickable(false);
            recordPart.setFocusable(false);
            recordPart.setOnClickListener(null);
            recordPart.setVisibility(View.INVISIBLE);

            LinearLayout controlPanel = findViewById(R.id.control_panel);
            controlPanel.setVisibility(View.VISIBLE);

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

    public void onDeleteButtonClick(View view) {
        UIHelpers.showConfirmationDialog(this, "Delete Recording",
                "Are you sure you want to delete this recording?",
                new UIHelpers.ConfirmationDialogListener() {
                    @Override
                    public void onConfirmed() {
                        deleteRecording();
                    }

                    @Override
                    public void onCancelled() {
                        Log.d("RecordActivity", "Deletion cancelled");
                    }
                });
    }

    private void deleteRecording() {
        if (currentRecording != null && currentRecording.getFilePath() != null) {
            boolean isDeleted = audioManager.deleteRecording(currentRecording.getFilePath());

            if (isDeleted) {
                resetRecordingState();
                Log.d("RecordActivity", "Delete recording successfully");
                Toast.makeText(this, "Recording deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("RecordActivity", "Failed to delete recording");
                Toast.makeText(this, "Failed to delete recording", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("RecordActivity", "No recording to delete");
        }
    }

    public void onNextButtonClick(View view) {
        resetRecordingState();
    }

    private void resetRecordingState() {
        // Reset UI
        TextView recordStatusText = findViewById(R.id.record_status_text);
        TextView recordingTimeView = findViewById(R.id.recording_time_text);
        recordStatusText.setText(R.string.tap_to_record);
        recordingTimeView.setVisibility(View.INVISIBLE);
        recordStatusText.setVisibility(View.VISIBLE);

        // Reset record part to be clickable
        FrameLayout recordPart = findViewById(R.id.record_part);
        recordPart.setClickable(true);
        recordPart.setFocusable(true);
        recordPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecordButtonClick(v);
            }
        });
        recordPart.setVisibility(View.VISIBLE);

        // Hide control panel
        LinearLayout controlPanel = findViewById(R.id.control_panel);
        controlPanel.setVisibility(View.GONE);

        // Clear recording time
        handler.removeCallbacks(updateRecordingTimeRunnable);

        // Reset recording state
        isRecordingStarted = false;

        // Set currentRecording to null
        currentRecording = null;

        Log.d("RecordActivity", "Reset recording state");
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

