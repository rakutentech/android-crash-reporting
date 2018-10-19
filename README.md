# Crash Reporting
[![CircleCI](https://circleci.com/gh/rakutentech/android-crash-reporting/tree/master.svg?style=svg)](https://circleci.com/gh/rakutentech/android-crash-reporting/tree/master)

TODO: description

## Buliding

Set the environment variables `CRASH_CONFIG_API`, `CRASH_MAPPING_API` and `CRASH_SAMPLE_API_KEY` before building

```bash
export CRASH_CONFIG_API=https://your.config.api/endpoint
export CRASH_MAPPING_API=https://your.mapping.api/endpoint
export CRASH_SAMPLE_API_KEY=YOU_API_KEY

# This line expose the varaible to Android Studio and other GUI apps (on mac os x)
launchctl setenv CRASH_CONFIG_API $CRASH_CONFIG_API 
launchctl setenv CRASH_MAPPING_API $CRASH_MAPPING_API
launchctl setenv CRASH_SAMPLE_API_KEY $CRASH_SAMPLE_API_KEY

./gradlew assemble
```
