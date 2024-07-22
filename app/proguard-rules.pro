# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/josh/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-assumenosideeffects class androidx.compose.material.icons.extended.{
    !Visibility,
    !VisibilityOff,
    **
} {
    <methods>;
}

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {*;}
-keep class androidx.** {*;}
-keep interface androidx.** { *; }
-keep class androidx.core.app.CoreComponentFactory { *; }