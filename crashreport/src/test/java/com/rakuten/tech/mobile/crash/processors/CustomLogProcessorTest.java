package com.rakuten.tech.mobile.crash.processors;

import static org.junit.Assert.*;

import com.rakuten.tech.mobile.crash.CrashReport;
import com.rakuten.tech.mobile.crash.exception.LogEntrySizeLimitExceededError;
import java.util.Arrays;
import org.junit.Test;

/**
 * Unit tests for CustomLogProcessor.java.
 */
public class CustomLogProcessorTest {

  @Test
  public void addNullCustomLog() throws LogEntrySizeLimitExceededError {
    // Allow log messages set to null, but it will not be included in the list of logs.
    CrashReport.getInstance().log(null);
    assertTrue(CustomLogProcessor.getInstance().getCustomLogs().isEmpty());
  }

  @Test(expected = LogEntrySizeLimitExceededError.class)
  public void addLargeCustomLog() throws LogEntrySizeLimitExceededError {
    // Reject custom logs that reaches the 1KB limit or greater.
    char[] chars = new char[1000];
    Arrays.fill(chars, 'a');

    CrashReport.getInstance().log(new String(chars));
  }

  @Test
  public void noCustomLog() throws LogEntrySizeLimitExceededError {
    assertTrue(CustomLogProcessor.getInstance().getCustomLogs().isEmpty());
  }

  @Test
  public void addCustomLog() throws LogEntrySizeLimitExceededError {
    CrashReport.getInstance().log("TEST_LOG");

    assertFalse(CustomLogProcessor.getInstance().getCustomLogs().isEmpty());
  }

  @Test
  public void customLogsAddedInOrder() throws LogEntrySizeLimitExceededError {
    // Allow insertion of at most 64 log messages.
    for (int i = 0; i < 64; i++) {
      CrashReport.getInstance().log(Integer.toString(i));
    }

    String[] logList = CustomLogProcessor.getInstance().getCustomLogs().split("\r\n");

    // Assert that 64 keys are stored in order.
    for (Integer i = 0; i < 64; i++) {
      assertTrue(logList[i].equals(i.toString()));
    }
  }

  @Test
  public void arbitraryCustomLogsAddedInOrder() throws LogEntrySizeLimitExceededError {
    // Allow inserting 70 log messages; Removes the oldest log messages after 64.
    for (int i = 0; i < 70; i++) {
      CrashReport.getInstance().log(Integer.toString(i));
    }

    String[] logList = CustomLogProcessor.getInstance().getCustomLogs().split("\r\n");

    // Check that all keys are stored in expected order after exceeding 64 keys.
    for (Integer i = 6; i < 64; i++) {
      // Assert true that the first 6 keys no longer contains 0 to 5, since they were removed.
      assertTrue(logList[i - 6].equals(i.toString()));
    }
  }
}