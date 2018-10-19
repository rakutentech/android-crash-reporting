package com.rakuten.tech.mobile.crash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import com.rakuten.tech.mobile.crash.tasks.FlushLifecyclesTask;

/**
 * Broadcast receiver that listens to changes in network state and acts according to the flags it reads.
 * In particular attempts to send data to the server when previous attempt could not be completed due to lack of connectivity.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(final Context context, Intent intent) {
    // NetworkInfo is null in airplane mode.
    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

    // Checks if device is connected to a network and ignores disconnection.
    if (networkInfo != null
        && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {

      // Delays lifecycle cache flushing for 5 seconds for device to be fully connected to the internet.
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {

          // Initiate the SDK when it was unable to be initiated while app in foreground.
          if (PreferenceManager.getDefaultSharedPreferences(context)
              .getBoolean(CrashReportConstants.FAILED_INIT, false)) {

            CrashReport.getInstance().init(context);
          }

          // Flushes lifecycles when flag is set to flush.
          if (PreferenceManager
              .getDefaultSharedPreferences(context).getBoolean(CrashReportConstants
                  .FLUSH_LIFECYCLES, false)) {

            queueTaskInBackground(new FlushLifecyclesTask());
          }
        }
      }, 5000);
    }
  }

  /**
   * Checks if device is connected to a network to proceed in communicating with config server.
   */
  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivityManager
        = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    return connectivityManager.getActiveNetworkInfo() != null
        && connectivityManager.getActiveNetworkInfo().isConnected();
  }

  /**
   * Queue crash reporting tasks asynchronously to ensure crash reporting does not affect the app performance on the main thread.
   */
  private void queueTaskInBackground(final CrashReportTask task) {
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... voids) {
        CrashReportTaskQueue.getInstance().enqueue(task);
        return null;
      }
    }.execute();
  }
}
