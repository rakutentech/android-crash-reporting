package com.rakuten.tech.mobile.crash;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import com.rakuten.tech.mobile.crash.AsyncHttpPost.CrashServerURL;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for AsyncHttpPost.java.
 */
@RunWith(MockitoJUnitRunner.class)
public class AsyncHttpPostTest {

  private static final String POST_FIELD = "POST";
  private static final String CONTENT_TYPE_FIELD = "Content-Type";
  private static final String APP_CHARSET_FIELD = "application/json; charset=utf-8";
  private static final String SUBSCRIPTION_ID = "Ocp-Apim-Subscription-Key";
  @Mock CrashServerURL mockURL;
  @Mock JSONObject mockJsonObject;
  @Mock HttpURLConnection mockConnection;
  @Mock OutputStreamWriter mockOutputStreamWriter;
  @Mock OutputStream mockOutputStream;
  private AsyncHttpPost asyncHttpPost;

  /**
   * Initializes an AsyncHttpPost object with a mock url and jsonObject.
   */
  @Before
  public void setup() {
    asyncHttpPost = new AsyncHttpPost(mockURL, mockJsonObject);
  }

  /**
   * Verify if AsyncHttpPost execute() has correctly connected to crash server.
   */
  @Test
  public void testExecute() throws IOException {
    when(mockURL.openConnection()).thenReturn(mockConnection);
    when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);
    when(mockConnection.getInputStream())
        .thenReturn(new ByteArrayInputStream("".getBytes("UTF-8")));
    asyncHttpPost.doInBackground();
    verify(mockConnection).setRequestProperty(eq(CONTENT_TYPE_FIELD), eq(APP_CHARSET_FIELD));
    verify(mockConnection).setRequestProperty(SUBSCRIPTION_ID, "");
    verify(mockConnection).setRequestMethod(eq(POST_FIELD));
    verify(mockConnection).setDoOutput(true);
    verify(mockConnection).setDoInput(true);
    verify(mockConnection).connect();
    verify(mockConnection).getResponseCode();
    verify(mockConnection).disconnect();
  }

  /**
   * Verify if AsyncHttpPost execute() can handle an IOException while creating a connection to crash server.
   */
  @Test
  public void testExecuteThrowsIOException() throws IOException {
    when(mockURL.openConnection()).thenThrow(new IOException());
    asyncHttpPost.doInBackground();
    verifyNoConnectionInteraction();
  }

  /**
   * Tests if AsyncHttpPost execute() has correct functionality during an IOException.
   */
  @Test
  public void verifyNoConnectionInteraction() throws IOException {
    verify(mockConnection, times(0))
        .setRequestProperty(eq(CONTENT_TYPE_FIELD), eq(APP_CHARSET_FIELD));
    verify(mockConnection, times(0)).setRequestMethod(eq(POST_FIELD));
    verify(mockConnection, times(0)).setDoOutput(true);
    verify(mockConnection, times(0)).setDoInput(true);
    verify(mockConnection, times(0)).connect();
    verify(mockConnection, times(0)).getResponseCode();
    verify(mockConnection, times(0)).getErrorStream();
    verify(mockConnection, times(0)).getResponseMessage();
    verify(mockConnection, times(0)).disconnect();
  }
}
