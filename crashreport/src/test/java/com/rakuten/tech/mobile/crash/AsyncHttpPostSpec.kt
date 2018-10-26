package com.rakuten.tech.mobile.crash

import com.rakuten.tech.mobile.crash.AsyncHttpPost.CrashServerURL
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URLConnection

class AsyncHttpPostSpec : RobolectricUnitSpec() {
    @Mock
    internal lateinit var stubURL: CrashServerURL
    @Mock
    internal lateinit var mockConnection: HttpURLConnection
    @Mock
    internal lateinit var mockOutputStream: OutputStream

    private lateinit var asyncHttpPost: AsyncHttpPost

    @Before
    fun setup() {
        asyncHttpPost = AsyncHttpPost(stubURL, JSONObject("{}"))
    }

    @Test
    fun `should do a lot of things`() { // TODO: this test conflates many behaviors. should split

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

    @Test
    fun `should should handle IOExceptions`() {
        `when`<URLConnection>(stubURL.openConnection()).thenThrow(IOException())

        asyncHttpPost.doInBackground()

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

    @Test
    fun `should not start HTTP request withou explicit call to doInBackground`() {
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
