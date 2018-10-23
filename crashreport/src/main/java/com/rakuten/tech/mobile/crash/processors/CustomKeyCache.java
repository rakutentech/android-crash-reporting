package com.rakuten.tech.mobile.crash.processors;

import android.support.annotation.VisibleForTesting;
import com.rakuten.tech.mobile.crash.CrashReportConstants;
import com.rakuten.tech.mobile.crash.exception.IllegalKeyValuePairException;
import com.rakuten.tech.mobile.crash.exception.KeyValuePairSizeExceededError;
import com.rakuten.tech.mobile.crash.exception.MaximumCapacityReachedError;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds arbitrary key/value pairs to crash details for debugging purposes.
 * @hide
 */
public class CustomKeyCache {

  private static final CustomKeyCache INSTANCE = new CustomKeyCache();
  private static final Integer MAX_SIZE = 64; /* Maximum number of custom keys allowed. */
  private Map<String, String> customKeysMap = new HashMap<>();

  @VisibleForTesting
  CustomKeyCache() {}

  public static CustomKeyCache getInstance() {
    return INSTANCE;
  }

  /**
   * Adds a custom key/value pair to custom key map.
   */
  public void addCustomKey(String key, String value)
      throws KeyValuePairSizeExceededError, IllegalKeyValuePairException, MaximumCapacityReachedError {

    // Passing a null key/value pair to custom key map.
    if (key == null) {
      throw new IllegalKeyValuePairException();
    }

    // Reached the maximum number of custom keys.
    if (customKeysMap.size() == MAX_SIZE) {
      throw new MaximumCapacityReachedError();
    }

    // Adds or updates a key/value pair under 1KB in size to the custom key map.
    if (value != null && key.getBytes().length + value.getBytes().length < 1000) {
      customKeysMap.put(key, value);
    } else if (value == null && key.getBytes().length < 1000) {
      // Adds a key with a value representing null as a string.
      customKeysMap.put(key, CrashReportConstants.NULL);
    } else {
      throw new KeyValuePairSizeExceededError();
    }
  }

  /**
   * Removes a custom key/value pair from custom key map.
   */
  public void removeCustomKey(String key) {
    // Removes an existing key from the custom key map.
    if (key != null) {
      customKeysMap.remove(key);
    }
  }

  /**
   * Default access for sessionsLifecycleProcessor to retrieve all custom keys.
   */
  Map<String, String> getCustomKeys() {
    return customKeysMap;
  }
}
