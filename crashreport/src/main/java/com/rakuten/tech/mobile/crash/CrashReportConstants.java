package com.rakuten.tech.mobile.crash;

/**
 * Collection of constants used to communicate with Crash Reporting.
 */
public class CrashReportConstants {

  // Shared Preference constants created for the SDK.
  public static final String FAILED_INIT = "FAILED_INIT";
  public static final String FLUSH_LIFECYCLES = "FLUSH_LIFECYCLES";
  public static final String NEW_INSTALL = "NEW_INSTALL";

  // Files located in the Android file system.
  public static final String CPUINFO_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
  public static final String MEMORYINFO_FILE = "/proc/meminfo";
  public static final String LIFECYCLE_FILE = "lifecycle.txt";

  // Request fields used to store android device related information.
  public static final String APP_ID = "app_id";
  public static final String APP_EVENTS = "app_events";
  public static final String APP_KEY = "app_key";
  public static final String APP_VALUE = "app_value";
  public static final String APP_VERSION = "app_version";
  public static final String ANDROID = "Android";
  public static final String BG = "bg";
  public static final String CARRIER = "carrier";
  public static final String CPU = "CPU";
  public static final String CRASH_DETAILS = "crash_details";
  public static final String DEVICE_ID = "device_id";
  public static final String DEVICE_INFO = "device_info";
  public static final String FG = "fg";
  public static final String LIFECYCLES = "lifecycles";
  public static final String LOCALE = "locale";
  public static final String MAKE = "make";
  public static final String MEMORY = "memory";
  public static final String MODEL = "model";
  public static final String NULL = "null";
  public static final String OS = "os";
  public static final String OS_VERSION = "os_version";
  public static final String ORIGIN_ERROR = "origin_error";
  public static final String PLATFORM = "platform";
  public static final String PROCESSOR = "processor";
  public static final String SDK_VERSION = "sdk_version";
  public static final String STACK_TRACE = "stack_trace";
  public static final String STATUS = "status";
  public static final String SYS_KEY = "sys_key";
  public static final String SYS_VALUE = "sys_value";
  public static final String SYSTEM_STATS = "system_stats";
  public static final String THREADS_KEY = "Thread: ";
  public static final String VERSION = "version";

  // Subscription ID Key which host app must also use the same name.
  public static final String SUBSCRIPTION_KEY = "com.rakuten.tech.mobile.relay.SubscriptionKey";
  // Header key in every request.
  public static final String HEADER_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";

  // Configuration server fields.
  public static final String DATA = "data";
  public static final String ENABLED = "enabled";
  public static final String ENDPOINTS = "endpoints";
  public static final String INSTALL = "install";
  public static final String OVERRIDE = "override";
  public static final String SESSIONS = "sessions";
  public static final String STICKY = "sticky";

  // System stats fields.
  public static final String DEVICE_MODEL= "device_model";
  public static final String FREE_DISK_SPACE = "free_disk_space";
  public static final String FREE_RAM = "free_ram";
  public static final String IS_APP_IN_FOCUS = "is_app_in_focus";
  public static final String THREADS = "threads";
  public static final String TOTAL_DISK_SPACE = "total_disk_space";
  public static final String TOTAL_RAM = "total_ram";

  // App Key Events.
  public static final String LOG = "log";
}