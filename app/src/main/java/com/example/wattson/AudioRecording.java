package com.example.wattson;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class AudioRecording {
    private String fileName;
    private String filePath;
    private long duration;
    private boolean isRecording;

    public AudioRecording(String fileName, String filePath, long duration, boolean isRecording) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.duration = duration;
        this.isRecording = isRecording;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recordingStatus) {
        isRecording = recordingStatus;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedDuration() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public long getFileSize() {
        File file = new File(filePath);
        return file.length(); // Returns size in bytes
    }

    @NonNull
    @Override
    public String toString() {
        return "AudioRecording{" +
                "fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", duration=" + duration +
                '}';
    }

}

