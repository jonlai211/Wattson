package com.example.wattson;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Locale;

public class RecordingPlaybackActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private boolean isPlaying = false;
    private Handler handler = new Handler();
    private SeekBar seekBar;
    private TextView playbackTimeText;
    private String recordingFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        audioManager = new AudioManager(this);
        recordingFileName = getIntent().getStringExtra("RECORDING_FILE_NAME");

        ImageButton playButton = findViewById(R.id.play_button);
        ImageButton deleteButton = findViewById(R.id.delete_button);
        seekBar = findViewById(R.id.seek_bar);
        playbackTimeText = findViewById(R.id.playback_time_text);

        playButton.setOnClickListener(v -> {
            if (!isPlaying) {
                audioManager.playRecording(recordingFileName);
                isPlaying = true;
                playButton.setImageResource(R.drawable.baseline_pause_48);
                updateSeekBar();
            } else {
                audioManager.pausePlayback();
                isPlaying = false;
                playButton.setImageResource(R.drawable.baseline_play_arrow_48);
            }
        });

        deleteButton.setOnClickListener(v -> confirmAndDeleteRecording());

        seekBar.setMax(audioManager.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.seekTo(progress);
                    updatePlaybackTime(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 可选：用户开始拖动 SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 可选：用户停止拖动 SeekBar
            }
        });
    }

    private void updateSeekBar() {
        runOnUiThread(() -> {
            if (isPlaying) {
                int currentPosition = audioManager.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                updatePlaybackTime(currentPosition);
                handler.postDelayed(this::updateSeekBar, 1000);
            }
        });
    }

    private void updatePlaybackTime(int progress) {
        int seconds = (progress / 1000) % 60;
        int minutes = (progress / (1000 * 60));
        playbackTimeText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void confirmAndDeleteRecording() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Recording")
                .setMessage("Are you sure you want to delete this recording?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (audioManager.deleteRecording(recordingFileName)) {
                        Toast.makeText(this, "Recording deleted successfully", Toast.LENGTH_SHORT).show();
                        finish(); // 关闭活动
                    } else {
                        Toast.makeText(this, "Failed to delete recording", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlaying) {
            audioManager.pausePlayback();
            isPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioManager.stopPlayback();
        handler.removeCallbacksAndMessages(null);
    }
}
