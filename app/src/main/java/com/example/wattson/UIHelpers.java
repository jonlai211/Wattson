package com.example.wattson;

import android.content.Context;
import android.widget.Toast;

public class UIHelpers {
    private Context context;

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void updateRecordingTime(String time) {
        // 更新 UI 以显示时间
    }

    // 其他UI辅助方法，如显示/隐藏进度条、动画效果等
}

