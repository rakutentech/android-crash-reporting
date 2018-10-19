package com.rakuten.tech.mobile.crash.processors;

import static org.junit.Assert.*;

import com.rakuten.tech.mobile.crash.exception.IllegalKeyValuePairException;
import com.rakuten.tech.mobile.crash.exception.KeyValuePairSizeExceededError;
import com.rakuten.tech.mobile.crash.exception.MaximumCapacityReachedError;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Unit tests for CustomKeyProcessor.java.
 */
public class CustomKeyProcessorTest {

  private Map<String, String> customKeysMap;
  private final String TEST_KEY = "test_key1";
  private final String TEST_VALUE = "test_value";

  @Test(expected = IllegalKeyValuePairException.class)
  public void addNullCustomKeyToMap1() throws KeyValuePairSizeExceededError,
      IllegalKeyValuePairException, MaximumCapacityReachedError {
    // Assert true that map will not contain null custom key.
    CustomKeyProcessor.getInstance().addCustomKey(null, TEST_VALUE);
  }

  @Test(expected = KeyValuePairSizeExceededError.class)
  public void rejectLargeCustomKeyFromMap() throws KeyValuePairSizeExceededError,
      IllegalKeyValuePairException, MaximumCapacityReachedError {
    // Reject custom key that reaches the 1KB limit or greater.
    char[] chars = new char[1000];
    Arrays.fill(chars, 'a');

    CustomKeyProcessor.getInstance().addCustomKey(TEST_KEY, new String(chars));
  }

  @Test
  public void addNullCustomKeyToMap() throws KeyValuePairSizeExceededError,
      IllegalKeyValuePairException, MaximumCapacityReachedError {
    // Assert true that map will contain custom key with a null string value.
    CustomKeyProcessor.getInstance().addCustomKey(TEST_KEY, null);

    customKeysMap = new HashMap<>(CustomKeyProcessor.getInstance().getCustomKeys());
    assertTrue(customKeysMap.containsKey(TEST_KEY));
  }

  @Test
  public void removeNullKeyFromMap() {
    // Should not make any changes to the custom map.
    CustomKeyProcessor.getInstance().removeCustomKey(null);
  }

  @Test
  public void addAndRemoveCustomKeyFromMap() throws KeyValuePairSizeExceededError,
      IllegalKeyValuePairException, MaximumCapacityReachedError {
    // Assert true that custom key is inserted and map contains the key.
    CustomKeyProcessor.getInstance().addCustomKey(TEST_KEY, TEST_VALUE);

    // Map contains size 1.
    customKeysMap = new HashMap<>(CustomKeyProcessor.getInstance().getCustomKeys());
    assertTrue(customKeysMap.size() == 1);

    // Assert false that map does contain key; After key removal by setting value to null.
    CustomKeyProcessor.getInstance().removeCustomKey(TEST_KEY);
    customKeysMap = new HashMap<>(CustomKeyProcessor.getInstance().getCustomKeys());
    assertFalse(customKeysMap.containsKey(TEST_KEY));

    // Map contains size 0.
    customKeysMap = new HashMap<>(CustomKeyProcessor.getInstance().getCustomKeys());
    assertTrue(customKeysMap.size() == 0);
  }

  @Test
  public void getCustomValueFromMap() throws KeyValuePairSizeExceededError,
      IllegalKeyValuePairException, MaximumCapacityReachedError {
    // Assert true that custom key can be used to retrieve its value from map.
    CustomKeyProcessor.getInstance().addCustomKey(TEST_KEY, TEST_VALUE);

    customKeysMap = new HashMap<>(CustomKeyProcessor.getInstance().getCustomKeys());
    assertTrue(customKeysMap.containsKey(TEST_KEY));
    assertTrue(customKeysMap.get(TEST_KEY).equals(TEST_VALUE));

    customKeysMap = new HashMap<>(CustomKeyProcessor.getInstance().getCustomKeys());
    assertTrue(customKeysMap.size() == 1);

    // Remove key from map.
    CustomKeyProcessor.getInstance().removeCustomKey(TEST_KEY);
  }
}