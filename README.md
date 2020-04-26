# Cisco Webex Android SDK Example

This "Kitchen Sink" demo employs Cisco Webex service through [Webex Android SDK](https://github.com/webex/webex-android-sdk).  It provides a developer friendly sample implementation of Webex client SDK and showcases all SDK features. It focuses on how to call and use "Webex-SDK" APIs. Developers could directly cut, paste, and use the code from this sample. It basically implements “Webex-SDK” APIs by sequence.

This demo support Android device with **Android 6.0** or later

## Table of Contents

- [Setup](#setup)
- [Install](#install)
- [Usage](#usage)


## Screenshots 
<ul>
<img src="https://github.com/jpjpjp/webex-android-sdk-example/blob/master/docs/Screenshot_Kitchensink_Android_MainPage.jpg" width="22%" height="23%">
<img src="https://github.com/jpjpjp/webex-android-sdk-example/blob/master/docs/Screenshot_Kitchensink_Android_CallPage.jpg" width="22%" height="23%">
<img src="https://github.com/jpjpjp/webex-android-sdk-example/blob/master/docs/Screenshot_20180907-164030.png" width="22%" height="23%">
<img src="https://github.com/jpjpjp/webex-android-sdk-example/blob/master/docs/Screenshot_Kitchensink_Android_MessagePage.jpg" width="22%" height="23%">
</ul>

1. ScreenShot-1: Main page of Application, listing main functions of this demo.
2. ScreenShot-2: Initiate call page, contains call configuration options.
3. ScreenShot-3: Calling room with multi-stream view.
4. ScreenShot-4: Show messaing APIs with present payloads.

## Setup

- Install Java SE Development Kit 7u45 or later (Java 8 is OK)

  http://www.oracle.com/technetwork/java/javase/downloads/index.html

- Android Studio 2.3 or later

  https://developer.android.com/sdk/installing/studio.html
  
- Android Plugin for Gradle 3 or later
  
  If you upgrade your gradle plugin newer than 3.0.0, make sure to add google repository and remove retrolambda plugin in your build.gradle file:
  ```
  allprojects {
    repositories {
        jcenter()
        google()
        ...
    }
  }
  ```

- Select tools->Android->SDK Manager in Android Studio, and install the following packages:
  * Android SDK Tools
  * Android SDK Platform-Tools
  * Android SDK Build-Tools (latest)
  * Android 7.1.1 (latest)
  * Support Repository: Android Support Repository
  * Support Repository: ConstraintLayout for Android
  * Support Repository: Google Repository


## Install

1.  Clone or download project to your computer.
    ```
    git clone https://github.com/webex/webex-android-sdk-example
    ```

2. Import project 
In Android Studio,open "File" -> "Open", select project.

3. Connect your device with debug enabled

4. Build & Run

## Usage

### Authorize
**Webex ID**
If you have a Webex ID (your email address), you can choose "**Webex ID**" to authorize your device.

**Guest ID**
If you have a Guest ID (a string token), choose "**Guest ID**" in the first page, then you need to enter your App JWT token and login.
