# Cisco Spark Android SDK Example

> This demo employs Cisco Spark service through [Spark Android SDK](https://github.com/ciscospark/spark-android-sdk).
> This demo support Android device with **Android 6.0** or later

## Table of Contents

- [Setup](#setup)
- [Install](#install)
- [Usage](#usage)


## Setup

- Install Java SE Development Kit 7u45 or later (Java 8 is OK)
  http://www.oracle.com/technetwork/java/javase/downloads/index.html

- Android Studio 2.3 or later
  https://developer.android.com/sdk/installing/studio.html

- Select tools->Android->SDK Manager in Anroid Studio, and install the following packages:
  * Android SDK Tools
  * Android SDK Platform-Tools
  * Android SDK Build-Tools (latest)
  * Android 7.1.1 (latest)
  * Support Repository: Android Support Repository
  * Support Repository: ConstraintLayout fro Android


## Install

1.  Clone or download project to your computer.
    ```
    git clone https://github.com/ciscospark/spark-android-sdk-example
    ```

2. Import project 
In Android Studio,open "File" -> "Open", select project.

3. Connect your device with debug enabled

4. Build & Run

## Usage

### Authorize
**Spark ID**
If you have a Spark ID (your email address), you can choose "**Spark ID**" to authorize your device.

**APP ID**
If you have a APP ID (a string token), choose "**App ID**" in the first page, then you need to enter your App JWT token and login.
