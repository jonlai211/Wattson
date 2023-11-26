package com.example.wattson;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AudioManagerAndroidTest {

    private Context context;
    private AudioManager audioManager;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        audioManager = new AudioManager(context);
    }

    @Test
    public void testStartRecording() {
        //TODO: study AndroidTest knowledge
        AudioRecording recording = audioManager.startRecording();

        // 验证是否成功创建了 AudioRecording 实例
        assertNotNull("AudioRecording instance should not be null", recording);

        // 验证录音文件的路径是否已正确设置
        assertNotNull("Recording path should not be null", recording.getFilePath());
        assertFalse("Recording path should not be empty", recording.getFilePath().isEmpty());
    }


    // Others
}

