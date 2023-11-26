package com.example.wattson;

import android.content.Context;
import android.widget.Toast;

public class UIHelpers {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // 其他UI辅助方法，如显示/隐藏进度条、动画效果等
}

