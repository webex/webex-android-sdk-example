# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep public class com.webex.wseclient.**
-keepclassmembers class com.webex.wseclient.** {
*;
}

-keep public class com.cisco.webex.wme.**
-keepclassmembers class com.cisco.webex.wme.** {
*;
}

# com.cisco.wme.appshare
-keep public class com.cisco.wme.appshare.**
-keepclassmembers class com.cisco.wme.appshare.** {
*;
}

-keep public class com.webex.wme.**
-keepclassmembers class com.webex.wme.** {
*;
}

-keep public class com.cisco.jabber.system.utils.**
-keepclassmembers class com.cisco.jabber.system.utils.** {
*;
}

-keep class com.ciscowebex.androidsdk.OmniusWrapper.**
-keepclassmembers class com.ciscowebex.androidsdk.OmniusWrapper {
*;
}
-keepclassmembers class com.ciscowebex.androidsdk.OmniusWrapper.** {
*;
}

-keep public class com.ciscowebex.androidsdk.internal.IOmniusServiceBridge.**
-keepclassmembers class com.ciscowebex.androidsdk.internal.IOmniusServiceBridge {
*;
}
-keepclassmembers class com.ciscowebex.androidsdk.internal.IOmniusServiceBridge.** {
*;
}


-keep public class com.ciscowebex.androidsdk.utils.internal.ProxyHelper
-keep public class com.ciscowebex.androidsdk.utils.internal.ProxyHelper.**
-keepclassmembers class com.ciscowebex.androidsdk.utils.internal.ProxyHelper {
*;
}
-keepclassmembers class com.ciscowebex.androidsdk.utils.internal.ProxyHelper.** {
*;
}

-keep public class com.ciscowebex.androidsdk.internal.ResultImpl
-keep public class com.ciscowebex.androidsdk.internal.ResultImpl.**
-keepclassmembers public class com.ciscowebex.androidsdk.internal.ResultImpl {
*;
}
-keepclassmembers public class com.ciscowebex.androidsdk.internal.ResultImpl.** {
*;
}

-keep interface com.ciscowebex.androidsdk.Result
-keep interface com.ciscowebex.androidsdk.Result.**
-keepclassmembers interface com.ciscowebex.androidsdk.Result {
*;
}
-keepclassmembers interface com.ciscowebex.androidsdk.Result.** {
*;
}

-keep class com.ciscowebex.androidsdk.message.LocalFile{*;}
-keep public class com.ciscowebex.androidsdk.internal.IOmniusServiceBridge {*;}
-keep class com.ciscowebex.androidsdk.message.LocalFileWrapper { *; }
-keep class com.ciscowebex.androidsdk.message.MessageClient$ProgressHandler { *; }

-keep class com.ciscowebex.androidsdk.utils.internal.NetworkHelper
-keep class com.ciscowebex.androidsdk.utils.internal.NetworkHelper.**
-keepclassmembers class com.ciscowebex.androidsdk.utils.internal.NetworkHelper {
*;
}
-keepclassmembers class com.ciscowebex.androidsdk.utils.internal.NetworkHelper.** {
*;
}

-keep interface com.ciscowebex.androidsdk.CompletionHandler
-keep interface com.ciscowebex.androidsdk.CompletionHandler.**
-keepclassmembers class com.ciscowebex.androidsdk.CompletionHandler{
*;
}
-keepclassmembers class com.ciscowebex.androidsdk.CompletionHandler.** {
*;
}

-keep public enum com.ciscowebex.androidsdk.omniusenums.**{
*;
}

-keep public class com.ciscowebex.androidsdk.omniusmodels.**{
*;
}

-keep enum com.ciscowebex.androidsdk.utils.internal.NetTypes{
*;
}
-keep class  com.ciscowebex.androidsdk.phone.internal.RenderSink{
*;
}