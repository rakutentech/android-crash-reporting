import com.android.build.gradle.api.ApplicationVariant
import com.google.common.net.MediaType
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.params.ConnRoutePNames
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

class CrashReportingPlugin implements Plugin<Project> {
  static def logger = Logging.getLogger(this.class)

  @Override
  void apply(Project project) {

    def info = new Properties()
    info.load(CrashReportingPlugin.classLoader.getResourceAsStream('info.properties'))

    // Assume it's Staging build by default.
    def version = info.getProperty('version')
    def mappingEndpointUrl = info.getProperty('mappingEndpointUrl')
    def sdkMavenDependency = "com.rakuten.tech.mobile.crash:crash-reporting:${version}"

    project.configure(project) {

      // Include Crash Reporting SDK as a dependency of the Application.
      repositories {
        mavenLocal() // TODO: setup publish to jcenter
        // jcenter()
      }
      dependencies {
        compile sdkMavenDependency
      }

      project.("android").applicationVariants.all { ApplicationVariant variant ->

        // Make sure minify is enabled, otherwise exit
        if (!variant.buildType.minifyEnabled) {
          return
        }

        // Define uploadMappingTask name.
        def uploadMappingTaskName = "crashReporting${variant.name.capitalize()}"

        // Create a new uploadMappingTask to upload mapping file.
        def uploadMappingTask = project.task(uploadMappingTaskName).doLast {

          // Uploads the mapping file along with version.
          upload(
              "${mappingEndpointUrl}${variant.applicationId}",
              variant.mappingFile ?: null,
              "${variant.versionName}_${variant.versionCode}"
          )
        }

        // finalizedBy() will make sure this uploadMappingTask run after mapping.txt is created,
        // moreover, `variant.assemble` will include both assembleDebug/assembleRelease.
        variant.assemble.finalizedBy(uploadMappingTask)
      }
    }
  }

  /**
   * Upload a mapping file using /api/upload-mapping REST service.
   *
   * @param mappingFile
   * @param versionCode
   */
  private static void upload(String url, File mappingFile, String versionCode) {
    // If mapping file or version is null, there's no need to post anything to server.
    if (mappingFile == null || versionCode == null) {
      return
    }

    CloseableHttpClient httpClient = buildCloseableHttpClient()
    HttpPost post = new HttpPost(url)
    MultipartEntityBuilder entity = MultipartEntityBuilder.create()

    entity.addPart('file', new FileBody(mappingFile))
    entity.addPart('version', new StringBody(versionCode))
    post.addHeader("Accept", MediaType.JSON_UTF_8.toString())
    post.setEntity(entity.build())

    HttpResponse response = httpClient.execute(post)

    logger.debug("status code: " + response.statusLine.statusCode)
  }

  /**
   * Create http client with proxy support.
   *
   * @return DefaultHttpClient
   */
  private static CloseableHttpClient buildCloseableHttpClient() {
    HttpClientBuilder httpClient = HttpClientBuilder.create()

    def proxyHost = System.getProperty("http.proxyHost")

    if (proxyHost != null) {
      def proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"))
      def proxyUser = System.getProperty("http.proxyUser")

      if (proxyPort != null && proxyUser != null) {
        HttpHost proxy = new HttpHost(proxyHost, proxyPort)

        AuthScope authScope = new AuthScope(proxyUser, proxyPort)
        Credentials credentials = new UsernamePasswordCredentials(proxyUser, System.getProperty("http.proxyPassword"))

        httpClient.getCredentialsProvider().setCredentials(authScope, credentials)

        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy)
      }
    }

    return httpClient.build()
  }
}
