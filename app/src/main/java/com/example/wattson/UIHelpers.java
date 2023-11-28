package com.example.wattson;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class UIHelpers {
    private Context context;

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public interface ConfirmationDialogListener {
        void onConfirmed();
        void onCancelled();
    }

    public static void showConfirmationDialog(Context context, String title, String message,
                                              ConfirmationDialogListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onConfirmed();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onCancelled();
                    }
                })
                .show();
    }

    // Other UI related methods like show progress bar etc.
}

