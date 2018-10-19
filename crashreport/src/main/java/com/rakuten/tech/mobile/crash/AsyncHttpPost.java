package com.rakuten.tech.mobile.crash;

import static com.rakuten.tech.mobile.crash.CrashReportConstants.HEADER_SUBSCRIPTION_KEY;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import com.rakuten.tech.mobile.crash.utils.CommonUtil;
import com.rakuten.tech.mobile.crash.utils.DeviceInfoUtil;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONObject;

/**
 * Performs HTTP POST requests to the Crash Report server.
 */
public class AsyncHttpPost extends AsyncTask<Void, Void, Integer> {
  private final static String TAG = "AsyncHttpPost";
  private final CrashServerURL url;
  private final JSONObject data;
  private String responseBody;

  public AsyncHttpPost(CrashServerURL url, JSONObject data) {
    this.url = url;
    this.data = data;
  }

  /**
   * Wrapper class for URL.java. Implemented for the ease of testing.
   */
  public static class CrashServerURL {

    private final URL crashServerURL;

    /**
     * Set the Crash Server URL.
     */
    public CrashServerURL(URL url) {
      crashServerURL = url;
    }

    /**
     * Start a connection with a crash server.
     */
    URLConnection openConnection() throws IOException {
      return crashServerURL.openConnection();
    }
  }

  /**
   * Executes a post request to the Crash Report server containing a JSON body with device and/or crash data.
   */
  @Override
  protected Integer doInBackground(Void... Param) {
    // Default status: unable to connect to server.
    int statusCode = 503;

    try {
      HttpURLConnection client = (HttpURLConnection) url.openConnection();
      client.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      client.setRequestProperty(HEADER_SUBSCRIPTION_KEY,
          DeviceInfoUtil.getInstance().getApiKey());
      client.setDoOutput(true);
      client.setDoInput(true);
      client.setRequestMethod("POST");
      client.connect();

      OutputStreamWriter outputPost = new OutputStreamWriter(client.getOutputStream());
      outputPost.write(data.toString());
      outputPost.flush();
      outputPost.close();
      statusCode = client.getResponseCode();

      responseBody = CommonUtil.INSTANCE.readInputStreamToString(client.getInputStream());
      client.disconnect();
    } catch (IOException e) {
      Log.e(TAG, "Failed to send JSON data via POST request to server", e);
    }

    return statusCode;
  }

  @Nullable
  public String getServerMessage() {
    return responseBody;
  }
}
