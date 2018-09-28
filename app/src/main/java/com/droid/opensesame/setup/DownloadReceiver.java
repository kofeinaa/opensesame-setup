package com.droid.opensesame.setup;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        if (referenceId > 0) {
            Toast.makeText(context, context.getString(R.string.download_complete), Toast.LENGTH_SHORT)
                    .show();
        }
    }
}