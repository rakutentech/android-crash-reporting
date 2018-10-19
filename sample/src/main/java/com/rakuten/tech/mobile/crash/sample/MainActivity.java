package com.rakuten.tech.mobile.crash.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


/***
 * Sample App for Crash Tracking SDK, creates crashes various crashes for demonstration & testing.
 */
public class MainActivity extends AppCompatActivity {
  private String nullString = null;

  @SuppressWarnings("ConstantConditions")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.numeric_overflow).setOnClickListener(new View.OnClickListener() {
      /**
       * Creates an array with an overflowed integer value.
       * Results in an NegativeArraySizeException
       */
      @SuppressWarnings("NumericOverflow")
      public void onClick(View v) {
        int[] result = new int[Integer.MAX_VALUE + 1000000];
      }
    });

    findViewById(R.id.stack_overflow).setOnClickListener(new View.OnClickListener() {
      /**
       * Calls itself to create a stack overflow error.
       * Results in a StackOverFlowError.
       */
      @SuppressWarnings("InfiniteRecursion")
      public void onClick(View v) {
        onClick(v);
      }
    });

    findViewById(R.id.null_pointer).setOnClickListener(new View.OnClickListener() {
      /**
       * Creates NPE
       */
      @SuppressWarnings("ResultOfMethodCallIgnored")
      public void onClick(View v) {
        nullString.equals("nothing!");
      }
    });
  }
}