# Cisco Spark Android SDK Demo

This demo employs Cisco Spark service through Spark Android SDK.
This Demo is written in JAVA and requires Android devices with **Android 5** or later version.

## Table of Contents

- [Install](#install)
- [Usage](#Usage)


## Install

1.  Clone or download project to your computer.
Recommended development environment:
Hardware: Apple MAC, 
IDE: Android Studio 2.3

2. Import project 
In Android Studio,open "File" -> "Open", select project.

3. Run
This demo only support real Android devices.
This demo has been verified on Android 6 and Android 7 devices.

## Usage

### Authorize
**Spark ID**
if you have a Spark account(Spark ID), you can click "**Authorize OAuth2**" to authorize your device.

**APP ID**
if you have a Spark app account(APP ID), you need to add your App JWT token into the source code, at 

```Java
MainActivity.java

public boolean onOptionsItemSelected(MenuItem item) {
...
if (id == R.id.action_authorizeJWT) {

//put your JWT token here
String testJwtToken = "";

...
}
```    

After you compile your project, you can click "Authorize JWT" to use the pre-load APP ID to authorize your device.

please refer to this page
https://wiki.cisco.com/pages/viewpage.action?pageId=57035134


### Register
After your device has been successfully authorized, you can register your device to Spark service.

### Call service
After your device has been successfully registered, you can use Spark call services.

## API Example

Here are some examples of how to use the Android SDK in your app.

**- Dependency libraries.**

For current demo, try to reuse build.gradle's configuration.
We will list necessary libraries in future release.

**- Steps to create Spark SDK instance**

Your android application class extends SparkApplication

```Java
public class MyApplication extends SparkApplication
```
Create Spark and Phone object and set Call observer

```Java
mSpark = new Spark();
mActiveCallObserver = new MyCallObserver(this);
``` 

Remember to close Spark object.
```Java
@Override
protected void onDestroy() {
super.onDestroy();
this.mSpark.Close();
}
```   

**- Spark API**


- **Steps for OAuth authorized**
```Java
strategy = new OAuthWebViewStrategy(clientId, clientSec, redirect, scope, email, webview);

this.mSpark.init(strategy);

strategy.authorize(new AuthorizeListener() {
@Override
public void onSuccess() {
...}
@Override
public void onFailed() {
...}
}
```                    

- **Steps for JWT authorized**

```Java
//put your token here 
String testJwtToken = "";

strategy = new JWTStrategy(testJwtToken);

this.mSpark.getStrategy().authorize(new AuthorizeListener() { 
@Override 
public void onSuccess() {
...}
@Override 
public void onFailed() {
...}
}
```


- **Steps to Register**
```Java
this.mSpark.phone().register(new RegisterListener() {
@Override
public void onSuccess() {
...}

@Override
public void onFailed() {
...}
}

```
- **Steps to Make Make VideoCall/AudioCall**

DialObserver will return whether the dial operation is successful or failed,for example, wrong parameter, or dial before active dial end. 
DialObserver's only handle SDK check. For Call event, CallObserver need to be implemented.
```Java
CallOption options = new CallOption(CallOption.CallType.VIDEO, this.remoteView, this.localView);

this.mSpark.phone().dial(dialstring, options, new DialObserver() {
@Override
public void onSuccess(Call call) {
}

@Override
public void onFailed(ErrorCode errorCode) {
}
});
```

- **Steps to Handle incoming call**

Need to implement inteface IncomingCallObserver, implement its onIncomingCall(Call var1);

```Java
CallOption options = new CallOption(CallOption.CallType.VIDEO, this.remoteView, this.localView);

this.mSpark.phone().dial(dialstring, options, new DialObserver() {
@Override
public void onSuccess(Call call) {
}

@Override
public void onFailed(ErrorCode errorCode) {
}
});
```
- **Steps to Handle Call event**
To handle call event which is returned from Spark server,CallObserver need to be implemented. 
events include dial operation event, in call operation event, and android permission event

```Java
public class MyCallObserver implements com.ciscospark.phone.CallObserver

@Override
public void onRinging(Call call) {
....
}

@Override
public void onConnected(Call call) {
....
}

@Override
public void onDisconnected(Call call, DisconnectedReason reason) {
....
}

@Override
public void onMediaChanged(Call call, MediaChangeReason reason) {
....
}

@Override
public void onPermissionRequired(List<String> list) {
....
}

```
