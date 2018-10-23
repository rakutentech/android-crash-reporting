package com.rakuten.tech.mobile.crash.processors;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import com.rakuten.tech.mobile.crash.CrashReportConstants;
import com.rakuten.tech.mobile.crash.processors.ConfigProcessor.OnConfigSuccessCallback;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Instrumented unit tests for ConfigProcessor.java.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27)
public class ConfigProcessorTest {

  private Context context = RuntimeEnvironment.application;
  private JSONObject data;
  private Boolean isEnabled;
  private CountDownLatch latch;
  private OnConfigSuccessCallback callback = new OnConfigSuccessCallback() {
    @Override
    public void onSuccess(Context context, boolean isSdkEnabled,
        @Nullable String reportSessionsUrl,
        @Nullable String reportInstallsUrl) {
      latch.countDown();
      isEnabled = isSdkEnabled;
    }
  };

  @Before
  public void setUp() throws JSONException {
    data = new JSONObject();
    isEnabled = null;
    latch = new CountDownLatch(1);
    JSONObject endpoints = new JSONObject();
    endpoints.put(CrashReportConstants.INSTALL, "");
    endpoints.put(CrashReportConstants.SESSIONS, "");
    data.put(CrashReportConstants.ENDPOINTS, endpoints);

    // Remove STICKY enabled preference.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .remove(CrashReportConstants.STICKY).apply();
  }

  @Test
  public void testCheckEnable() throws JSONException, InterruptedException {
    // Check that crash report enabled on app start up - sdk enabled.
    data.put(CrashReportConstants.OVERRIDE, false);
    data.put(CrashReportConstants.STICKY, false);
    data.put(CrashReportConstants.ENABLED, true);
    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertTrue(isEnabled);
  }

  @Test
  public void testCheckDisable() throws JSONException, InterruptedException {
    // Check that crash report disabled on app start up - sdk disabled.
    data.put(CrashReportConstants.OVERRIDE, false);
    data.put(CrashReportConstants.STICKY, false);
    data.put(CrashReportConstants.ENABLED, false);
    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse(isEnabled);
  }

  @Test
  public void testEmptyJSONServerConfig() throws JSONException, InterruptedException {
    // Disable SDK when missing required JSON fields.
    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse("Null JSONObject", isEnabled);
  }

  @Test
  public void testCompleteJSONServerConfigs() throws JSONException, InterruptedException {
    // Enables SDK with all required JSON fields and enabled as true.
    data.put(CrashReportConstants.OVERRIDE, true);
    data.put(CrashReportConstants.STICKY, true);
    data.put(CrashReportConstants.ENABLED, true);

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertTrue(isEnabled);
  }

  @Test
  public void testPartialJSONServerConfigs() throws JSONException, InterruptedException {
    // test 1
    // SDK is not enabled when missing any required JSON fields.
    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse("Missing Override", isEnabled);

    // test 2
    latch = new CountDownLatch(1);
    data.put(CrashReportConstants.OVERRIDE, true);

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse("Missing Sticky", isEnabled);

    // test 3
    latch = new CountDownLatch(1);
    data.put(CrashReportConstants.STICKY, true);

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse("Missing Enabled", isEnabled);

    // test 4
    latch = new CountDownLatch(1);
    data.put(CrashReportConstants.ENABLED, true);
    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertTrue(isEnabled);
  }

  @Test
  public void testCheckEnable1() throws JSONException, InterruptedException {
    // Overrides current sticky enabled false to true - sdk enabled.
    data.put(CrashReportConstants.OVERRIDE, true);
    data.put(CrashReportConstants.STICKY, true);
    data.put(CrashReportConstants.ENABLED, true);

    // Preset a previous host app config.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putBoolean(CrashReportConstants.STICKY, false).apply();

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertTrue(isEnabled);
  }

  @Test
  public void testCheckEnable2() throws JSONException, InterruptedException {
    // Overrides current sticky enabled true to false - sdk disabled.
    data.put(CrashReportConstants.OVERRIDE, true);
    data.put(CrashReportConstants.STICKY, true);
    data.put(CrashReportConstants.ENABLED, false);

    // Preset a previous host app config.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putBoolean(CrashReportConstants.STICKY, true).apply();

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse(isEnabled);
  }

  @Test
  public void testCheckEnable3() throws JSONException, InterruptedException {
    // Overrides current sticky enabled true to true - sdk enabled.
    data.put(CrashReportConstants.OVERRIDE, true);
    data.put(CrashReportConstants.STICKY, false);
    data.put(CrashReportConstants.ENABLED, true);

    // Preset a previous host app config.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putBoolean(CrashReportConstants.STICKY, true).apply();

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertTrue(isEnabled);
  }

  @Test
  public void testCheckEnable4() throws JSONException, InterruptedException {
    // Overrides current sticky enabled true to true - sdk enabled.
    data.put(CrashReportConstants.OVERRIDE, true);
    data.put(CrashReportConstants.STICKY, false);
    data.put(CrashReportConstants.ENABLED, false);

    // Preset a previous host app config.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putBoolean(CrashReportConstants.STICKY, true).apply();

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse(isEnabled);
  }

  @Test
  public void testCheckEnable5() throws JSONException, InterruptedException {
    // No override current sticky enabled false - sdk disabled.
    data.put(CrashReportConstants.OVERRIDE, false);
    data.put(CrashReportConstants.STICKY, true);
    data.put(CrashReportConstants.ENABLED, true);

    // Preset a previous host app config.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putBoolean(CrashReportConstants.STICKY, false).apply();

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse(isEnabled);
  }

  @Test
  public void testCheckEnable6() throws JSONException, InterruptedException {
    // No override current sticky enabled true - sdk enabled.
    data.put(CrashReportConstants.OVERRIDE, false);
    data.put(CrashReportConstants.STICKY, false);
    data.put(CrashReportConstants.ENABLED, true);

    // Preset a previous host app config.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putBoolean(CrashReportConstants.STICKY, true).apply();

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertTrue(isEnabled);
  }

  @Test
  public void testCheckEnable7() throws JSONException, InterruptedException {
    // No override current sticky enabled false - sdk disabled.
    data.put(CrashReportConstants.OVERRIDE, false);
    data.put(CrashReportConstants.STICKY, true);
    data.put(CrashReportConstants.ENABLED, false);

    // Preset a previous host app config.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putBoolean(CrashReportConstants.STICKY, false).apply();

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertFalse(isEnabled);
  }

  @Test
  public void testCheckEnable8() throws JSONException, InterruptedException {
    // No override current sticky enabled true - sdk enabled.
    data.put(CrashReportConstants.OVERRIDE, false);
    data.put(CrashReportConstants.STICKY, false);
    data.put(CrashReportConstants.ENABLED, false);

    // Preset a previous host app config.
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putBoolean(CrashReportConstants.STICKY, true).apply();

    ConfigProcessor.getInstance().updateHostConfig(context, data, callback);

    latch.await(1, TimeUnit.SECONDS);
    assertTrue(isEnabled);
  }
}