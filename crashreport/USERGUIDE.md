# CrashReporting

## Introduction 
Real-time mobile app crash tracking/reporting. 
`crash-reporting` plugin includes a Gradle plugin, as well as the SDK runtime. 
The Plugin uploads Android obfuscation mapping file to Crash Reporting backend, so obfuscated stack trace can be de-obfuscated.
The SDK runtime will capture crash stack traces and send them to Crash Reporting backend.

## Software Requirements
- API 15 or higher

## Getting Started
### 1.  Add Crash Reporting Plugin to your Project

```groovy
buildscript {
  repositories {
    mavenLocal()
    // TODO: setup publish to jcenter
    // jcenter()
  }
  dependencies {
    classpath "com.rakuten.tech.mobile.crash:crash-reporting-plugin:${version}"
  }
}  

apply plugin: 'crash-reporting'
```

### 2. Configure Subscription Key in your app's Manifest

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <application>
    <meta-data 
            android:name="com.rakuten.tech.mobile.relay.SubscriptionKey"
            android:value="Your-Subscription-Id" />
  </application>
</manifest>
```
    
### 3. Make sure to turn on `minifyEnabled true`

```groovy
android {
  buildTypes {
    debug {
      minifyEnabled false
    }
        
    release {
      minifyEnabled true
      proguardFile getDefaultProguardFile('proguard-android.txt')
    }
  }
}
```

## Advanced Features & Usage
If Crash Reporting has successfully compiled in the app, it will work immediately when the app launches without inserting any further code.

### Custom Keys
Associate arbitrary key/value pairs with your crash reports.

```java
CrashReport.getInstance().setBoolean("boolean", true);
CrashReport.getInstance().setDouble("double", 1.2d);
CrashReport.getInstance().setFloat("float", 1.2f);
CrashReport.getInstance().setInteger("int", 1);
CrashReport.getInstance().setString("string", "hi there");
```

### Custom Logs
Associate logs with your crash reports.

```java
CrashReport.getInstance().log(String message);
```

### Setting up Proguard

```proguard
# Keep proper file name and line number, add the following
-keepattributes SourceFile,LineNumberTable

# If you are using custom exceptions, add this line to skip obfuscation for custom exception
-keep public class * extends java.lang.Exception

# In order to make your build run faster, add the following line

-keep class com.rakuten.tech.mobile.crashreport.** { *; }
-dontwarn com.rakuten.tech.mobile.crashreport.**
```

If you see the following error, it's because Gradle can't find the mapping file.

```
Execution failed for task ':gateway:crashReportingPlaystoreRelease'.
> java.io.FileNotFoundException: ../release/mapping.txt (No such file or directory)
```

* Try remove the following line from the proguard file

```
-printmapping mapping.txt
```

