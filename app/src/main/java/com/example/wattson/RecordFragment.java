package com.example.wattson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class RecordFragment extends Fragment {
    private AudioManager audioManager;
    private TextTranscriptionManager textTranscriptionManager;
    private AudioRecording currentRecording;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean isRecordingStarted = false;
    private boolean isPlaying = false;
    private final Handler handler = new Handler();
    private String currentQuestionPart;
    private String currentQuestionTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // Init AudioManager and TextTranscriptionManager
        audioManager = new AudioManager(getContext());
        textTranscriptionManager = new TextTranscriptionManager();

        // Set SeekBar and UI gadgets
        setupUI(view);

        // Display question from database
        displayFromDatabase(view);

        // Permission check
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        return view;
    }

    private void setupUI(View view) {
        //Click Listener
        FrameLayout recordPart = view.findViewById(R.id.record_part);
        recordPart.setOnClickListener(this::onRecordButtonClick);
        recordPart.setOnLongClickListener(this::onRecordButtonLongClick);

        ImageButton playButton = view.findViewById(R.id.play_button);
        playButton.setOnClickListener(this::onPlayButtonClick);

        ImageButton noteButton = view.findViewById(R.id.note_button);
        noteButton.setOnClickListener(this::onNoteButtonClick);

        ImageButton transcribeButton = view.findViewById(R.id.audio_to_text_button);
        transcribeButton.setOnClickListener(this::onTranscribeButtonClick);

        ImageButton deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(this::onDeleteButtonClick);

        ImageButton nextQuestionButton = view.findViewById(R.id.next_question_button);
        nextQuestionButton.setOnClickListener(this::onNextButtonClick);

        // Init SeekBar
        SeekBar seekBar = view.findViewById(R.id.seek_bar);
        seekBar.setMax(100); // Assume the duration of the recording is 100 seconds
        seekBar.setProgress(0); // Init progress to 0

        // Set SeekBar change listener
        audioManager.setPlaybackCompletionListener(new AudioManager.PlaybackCompletionListener() {
            @Override
            public void onPlaybackComplete() {
                isPlaying = false;
                ImageButton playButton = view.findViewById(R.id.play_button);
                playButton.setImageResource(R.drawable.baseline_play_arrow_24);
                seekBar.setProgress(0); // Reset progress to 0
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    TextView playbackTimeView = view.findViewById(R.id.playback_time_text);
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
    }

    // Update recording time by runnable
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

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying && isAdded()) {
                int currentPosition = audioManager.getCurrentPosition();
                SeekBar seekBar = getView().findViewById(R.id.seek_bar);
                seekBar.setProgress(currentPosition);
                updatePlaybackTime(currentPosition);
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void updateRecordingTime(long millis) {
        View view = getView();
        if (view != null) {
            TextView recordingTimeView = view.findViewById(R.id.recording_time_text);
            String time = formatTime(millis);
            recordingTimeView.setText(time);
        }
    }

    public void updatePlaybackTime(int progress) {
        if (isAdded()) {
            TextView playbackTimeView = getView().findViewById(R.id.playback_time_text);
            String time = formatTime(progress);
            playbackTimeView.setText(time);
        }
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / (1000 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public void onRecordButtonClick(View view) {
        TextView recordStatusText = view.findViewById(R.id.record_status_text);
        TextView recordingTimeView = view.findViewById(R.id.recording_time_text);

        if (!isRecordingStarted) {
            if (currentQuestionPart != null) {
                currentRecording = audioManager.startRecording(currentQuestionPart, currentQuestionTitle);
            }
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
        TextView recordingTimeView = getView().findViewById(R.id.recording_time_text);

        if (isRecordingStarted && currentRecording != null && !currentRecording.isRecording()) {
            audioManager.stopRecording(currentRecording);
            currentRecording.setRecording(false);
            isRecordingStarted = false;

            TextView recordStatusText = getView().findViewById(R.id.record_status_text);
            recordStatusText.setText(R.string.test_show_finish);
            recordingTimeView.setVisibility(View.INVISIBLE);
            recordStatusText.setVisibility(View.INVISIBLE);

            FrameLayout recordPart = getView().findViewById(R.id.record_part);
            if (recordPart != null) {
                recordPart.setClickable(false);
                recordPart.setFocusable(false);
                recordPart.setOnClickListener(null);
                recordPart.setVisibility(View.INVISIBLE);
            }

            LinearLayout seekBarLayout = getView().findViewById(R.id.seek_bar_layout);
            if (seekBarLayout != null) {
                seekBarLayout.setVisibility(View.VISIBLE);
            }

            LinearLayout controlPanel = getView().findViewById(R.id.control_panel);
            if (controlPanel != null) {
                controlPanel.setVisibility(View.VISIBLE);
            }

            handler.removeCallbacks(updateRecordingTimeRunnable);
            Log.d("RecordFragment", "Long click on record button");
            return true;
        }
        return false;
    }

    public void onPlayButtonClick(View view) {
        ImageButton playButton = getView().findViewById(R.id.play_button);
        LinearLayout seekBarLayout = getView().findViewById(R.id.seek_bar_layout);

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
        ImageButton playButton = getView().findViewById(R.id.play_button);
        playButton.setImageResource(R.drawable.baseline_play_arrow_24);

        LinearLayout seekBarLayout = getView().findViewById(R.id.seek_bar_layout);
        if (seekBarLayout != null) {
            seekBarLayout.setVisibility(View.GONE);
        }

        SeekBar seekBar = getView().findViewById(R.id.seek_bar);
        seekBar.setProgress(0);
    }

    private void pausePlaybackIfNeeded() {
        View rootView = getView();
        if (rootView != null) {
            ImageButton playButton = rootView.findViewById(R.id.play_button);
            if (isPlaying) {
                audioManager.pausePlayback();
                playButton.setImageResource(R.drawable.baseline_play_arrow_24);
                isPlaying = false;
            }
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
        Context context = getContext();
        if (context != null) {
            UIHelpers.showConfirmationDialog(context, "Delete Recording",
                    "Are you sure you want to delete this recording?",
                    new UIHelpers.ConfirmationDialogListener() {
                        @Override
                        public void onConfirmed() {
                            deleteRecording();
                            View rootView = getView();
                            displayFromDatabase(rootView);
                        }

                        @Override
                        public void onCancelled() {
                            Log.d("RecordFragment", "Deletion cancelled");
                        }
                    });
        }
    }

    private void deleteRecording() {
        if (currentRecording != null && currentRecording.getFilePath() != null) {
            boolean isDeleted = audioManager.deleteRecording(currentRecording.getFilePath());
            Context context = getContext();

            if (isDeleted) {
                resetRecordingState();
                resetPlaybackUI();
                Log.d("RecordFragment", "Delete recording successfully");
                if (context != null) {
                    Toast.makeText(context, "Recording deleted successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("RecordFragment", "Failed to delete recording");
                if (context != null) {
                    Toast.makeText(context, "Failed to delete recording", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.d("RecordFragment", "No recording to delete");
        }
    }

    public void onNextButtonClick(View view) {
        audioManager.stopPlayback();
        pausePlaybackIfNeeded();
        resetPlaybackUI();
        resetRecordingState();

        View rootView = getView();
        if (rootView != null) {
            displayFromDatabase(rootView);
        } else {
            Log.e("RecordFragment", "Root view is null in onNextButtonClick");
        }
    }

    private void resetRecordingState() {
        View rootView = getView();
        // Reset UI
        TextView recordStatusText = rootView.findViewById(R.id.record_status_text);
        TextView recordingTimeView = rootView.findViewById(R.id.recording_time_text);
        recordStatusText.setText(R.string.tap_to_record);
        recordingTimeView.setVisibility(View.INVISIBLE);
        recordStatusText.setVisibility(View.VISIBLE);

        // Reset record part to be clickable
        FrameLayout recordPart = getView().findViewById(R.id.record_part);
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
        LinearLayout controlPanel = getView().findViewById(R.id.control_panel);
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
        View rootView = getView();
        LinearLayout seekBarLayout = rootView.findViewById(R.id.seek_bar_layout);
        SeekBar seekBar = rootView.findViewById(R.id.seek_bar);
        TextView playbackTimeView = rootView.findViewById(R.id.playback_time_text);

        seekBar.setProgress(0);
        playbackTimeView.setText(formatTime(0));
        seekBarLayout.setVisibility(View.GONE);

        handler.removeCallbacks(updateSeekBarRunnable);
    }

    public void stopAndReset() {
        if (isPlaying) {
            audioManager.stopPlayback();
            pausePlaybackIfNeeded();
            resetPlaybackUI();
        }
        if (isRecordingStarted) {
            audioManager.stopRecording(currentRecording);
            resetRecordingState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handle permission request result
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }

    private void displayFromDatabase(View view) {
        // Get the instance of DatabaseHelper
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getContext());

        // Get a random question
        String question = dbHelper.getRandomQuestion();

        if (question != null) {
            TextView questionTextView = view.findViewById(R.id.question_part);
            if (questionTextView != null) {
                questionTextView.setText(question);
                Log.d("RecordFragment", "Displaying question: " + question);
            } else {
                Log.e("RecordFragment", "Question TextView is null");
            }

            String questionId = dbHelper.getQuestionIdByContent(question);
            currentQuestionPart = dbHelper.getQuestionDetails(questionId, "part");
            currentQuestionTitle = dbHelper.getQuestionDetails(questionId, "title");

            // Now get the 'part' detail for this question
            String part = dbHelper.getQuestionDetails(questionId, "part");
            if (part != null) {
                TextView partTextView = view.findViewById(R.id.questionPart_part);
                if (partTextView != null) {
                    partTextView.setText(part);
                    Log.d("RecordFragment", "Displaying part: " + part);
                } else {
                    Log.e("RecordFragment", "Part TextView is null");
                }
            }
        }

        // ... rest of your code ...
    }


}
