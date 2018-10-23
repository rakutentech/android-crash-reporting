package com.rakuten.tech.mobile.crash

import com.rakuten.tech.mobile.crash.AsyncHttpPost.CrashServerURL

import java.net.HttpURLConnection
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.*
import java.net.URLConnection

/**
 * Unit tests for AsyncHttpPost.java.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(27))
class AsyncHttpPostSpec {
    @Mock
    internal lateinit var stubURL: CrashServerURL
    @Mock
    internal lateinit var mockConnection: HttpURLConnection
    @Mock
    internal lateinit var mockOutputStream: OutputStream

    private lateinit var asyncHttpPost: AsyncHttpPost

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        asyncHttpPost = AsyncHttpPost(stubURL, JSONObject("{}"))
    }

    /**
     * Verify if AsyncHttpPost execute() has correctly connected to crash server.
     */
    @Test
    @Throws(IOException::class)
    fun testExecute() {

        `when`<URLConnection>(stubURL.openConnection()).thenReturn(mockConnection)
        `when`(mockConnection.outputStream).thenReturn(mockOutputStream)
        `when`<InputStream>(mockConnection.inputStream)
                .thenReturn(ByteArrayInputStream("".toByteArray(charset("UTF-8"))))

        asyncHttpPost.doInBackground()

        verify<HttpURLConnection>(mockConnection).setRequestProperty(eq("Content-Type"), eq("application/json; charset=utf-8"))
        verify<HttpURLConnection>(mockConnection).setRequestProperty("Ocp-Apim-Subscription-Key", "")
        verify<HttpURLConnection>(mockConnection).requestMethod = eq("POST")
        verify<HttpURLConnection>(mockConnection).doOutput = true
        verify<HttpURLConnection>(mockConnection).doInput = true
        verify<HttpURLConnection>(mockConnection).connect()
        verify<HttpURLConnection>(mockConnection).responseCode
        verify<HttpURLConnection>(mockConnection).disconnect()
    }

    /**
     * Verify if AsyncHttpPost execute() can handle an IOException while creating a connection to crash server.
     */
    @Test
    @Throws(IOException::class)
    fun testExecuteThrowsIOException() {
        `when`<URLConnection>(stubURL.openConnection()).thenThrow(IOException())

        asyncHttpPost.doInBackground()

        verifyNoConnectionInteraction()
    }

    /**
     * Tests if AsyncHttpPost execute() has correct functionality during an IOException.
     */
    @Test
    @Throws(IOException::class)
    fun verifyNoConnectionInteraction() {
        verify<HttpURLConnection>(mockConnection, times(0))
                .setRequestProperty(eq("Content-Type"), eq("application/json; charset=utf-8"))
        verify<HttpURLConnection>(mockConnection, times(0)).requestMethod = eq("POST")
        verify<HttpURLConnection>(mockConnection, times(0)).doOutput = true
        verify<HttpURLConnection>(mockConnection, times(0)).doInput = true
        verify<HttpURLConnection>(mockConnection, times(0)).connect()
        verify<HttpURLConnection>(mockConnection, times(0)).responseCode
        verify<HttpURLConnection>(mockConnection, times(0)).errorStream
        verify<HttpURLConnection>(mockConnection, times(0)).responseMessage
        verify<HttpURLConnection>(mockConnection, times(0)).disconnect()
    }

}
