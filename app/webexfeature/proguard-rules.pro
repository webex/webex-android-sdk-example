# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Dynamic Feature Module (webexfeature) ProGuard Rules

# Keep DynamicModuleProvider - instantiated via reflection from base module
-keep class com.ciscowebex.androidsdk.kitchensink.DynamicModuleProvider {
    *;
}

# Keep KitchenSinkFCMService - instantiated via reflection for FCM handling
-keep class com.ciscowebex.androidsdk.kitchensink.firebase.KitchenSinkFCMService {
    *;
}

# Keep LoginActivity.LoginType enum for Koin module loading
-keep enum com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity$LoginType {
    *;
}

# Keep all Koin modules from being obfuscated
-keep class com.ciscowebex.androidsdk.kitchensink.**Module {
    *;
}
-keepnames class com.ciscowebex.androidsdk.kitchensink.**Module

# Keep Firebase messaging classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Webex SDK rules (same as base module)
-keep public class com.webex.wseclient.**
-keepclassmembers class com.webex.wseclient.** {
    *;
}

-keep public class com.cisco.webex.wme.**
-keepclassmembers class com.cisco.webex.wme.** {
    *;
}

-keep public class com.webex.wme.**
-keepclassmembers class com.webex.wme.** {
    *;
}

-keep public class com.ciscowebex.androidsdk.** {
    *;
}

# OkHttp optional TLS providers (absent in app; suppress warnings)
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
