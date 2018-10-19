package com.rakuten.tech.mobile.crash;

/**
 * Captures application state at time of crash before processing the report to backend servers.
 */
public enum ApplicationState {
  INSTANCE;
  public boolean isAppInFocus = false;
}
