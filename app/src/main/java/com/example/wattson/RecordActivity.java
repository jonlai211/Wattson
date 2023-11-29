package com.example.wattson;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class RecordActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private TextTranscriptionManager textTranscriptionManager;
    private AudioRecording currentRecording;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean isRecordingStarted = false;
    private boolean isPlaying = false;
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

        audioManager.setPlaybackCompletionListener(new AudioManager.PlaybackCompletionListener() {
            @Override
            public void onPlaybackComplete() {
                handlePlaybackCompletion();
            }
        });

        // Init SeekBar
        SeekBar seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(100); // Assume the duration of the recording is 100 seconds
        seekBar.setProgress(0); // Init progress to 0

        // Set SeekBar change listener
        audioManager.setPlaybackCompletionListener(new AudioManager.PlaybackCompletionListener() {
            @Override
            public void onPlaybackComplete() {
                isPlaying = false;
                ImageButton playButton = findViewById(R.id.play_button);
                playButton.setImageResource(R.drawable.baseline_play_arrow_24);
                seekBar.setProgress(0); // Reset progress to 0
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    TextView playbackTimeView = findViewById(R.id.playback_time_text);
                    String time = formatTime(progress);
                    playbackTimeView.setText(time);

                    audioManager.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: When user starts to drag the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: When user stops dragging the SeekBar
            }
        });

        // Bottom Navigation
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_record) {
                    return true;
                } else if (itemId == R.id.navigation_history) {
                    startActivity(new Intent(RecordActivity.this, HistoryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_library) {
                    startActivity(new Intent(RecordActivity.this, LibraryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    startActivity(new Intent(RecordActivity.this, AccountActivity.class));
                    return true;
                }
                return false;
            }
        });
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
        // Update UI
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

            LinearLayout seekBarLayout = findViewById(R.id.seek_bar_layout);
            seekBarLayout.setVisibility(View.VISIBLE);

            LinearLayout controlPanel = findViewById(R.id.control_panel);
            controlPanel.setVisibility(View.VISIBLE);

            handler.removeCallbacks(updateRecordingTimeRunnable);
            Log.d("RecordActivity", "Long click on record button");
            return true;
        }
        return false;
    }

    public void onPlayButtonClick(View view) {
        ImageButton playButton = findViewById(R.id.play_button);
        LinearLayout seekBarLayout = findViewById(R.id.seek_bar_layout);

        if (currentRecording != null && currentRecording.getFilePath() != null) {
            if (!isPlaying) {
                audioManager.playRecording(currentRecording.getFilePath());
                playButton.setImageResource(R.drawable.baseline_pause_24);
                seekBarLayout.setVisibility(View.VISIBLE);
                isPlaying = true;
                handler.post(updateSeekBarRunnable);
            } else {
                audioManager.pausePlayback();
                playButton.setImageResource(R.drawable.baseline_play_arrow_24);
                isPlaying = false;
                handler.removeCallbacks(updateSeekBarRunnable);
            }
        }
    }

    private void handlePlaybackCompletion() {
        isPlaying = false;
        ImageButton playButton = findViewById(R.id.play_button);
        playButton.setImageResource(R.drawable.baseline_play_arrow_24);

        SeekBar seekBar = findViewById(R.id.seek_bar);
        seekBar.setProgress(0);
    }

    private void pausePlaybackIfNeeded() {
        ImageButton playButton = findViewById(R.id.play_button);

        if (isPlaying) {
            audioManager.pausePlayback();
            playButton.setImageResource(R.drawable.baseline_play_arrow_24);
            isPlaying = false;
        }
    }

    public void onNoteButtonClick(View view) {
        pausePlaybackIfNeeded();
    }

    public void onTranscribeButtonClick(View view) {
        pausePlaybackIfNeeded();
    }

    public void onDeleteButtonClick(View view) {
        pausePlaybackIfNeeded();
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
                resetPlaybackUI();
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
        audioManager.stopPlayback();
        pausePlaybackIfNeeded();
        resetPlaybackUI();
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

    private void resetPlaybackUI() {
        LinearLayout seekBarLayout = findViewById(R.id.seek_bar_layout);
        SeekBar seekBar = findViewById(R.id.seek_bar);
        TextView playbackTimeView = findViewById(R.id.playback_time_text);

        seekBar.setProgress(0);
        playbackTimeView.setText(formatTime(0));
        seekBarLayout.setVisibility(View.GONE);

        handler.removeCallbacks(updateSeekBarRunnable);
    }

    // Other UI related methods
    private void updateRecordingTime(long millis) {
        TextView recordingTimeView = findViewById(R.id.recording_time_text);
        String time = formatTime(millis);
        recordingTimeView.setText(time);
    }

    public void updatePlaybackTime(int progress) {
        TextView playbackTimeView = findViewById(R.id.playback_time_text);
        String time = formatTime(progress);
        playbackTimeView.setText(time);
    }

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying) {
                int currentPosition = audioManager.getCurrentPosition();
                SeekBar seekBar = findViewById(R.id.seek_bar);
                seekBar.setProgress(currentPosition);
                updatePlaybackTime(currentPosition);
                handler.postDelayed(this, 1000);
            }
        }
    };

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

