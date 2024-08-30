# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/josh/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
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

# Keep XmlPullParser implementations
-keep class org.xmlpull.v1.** { *; }
-dontwarn org.xmlpull.v1.**

# Keep the Android internal XmlResourceParser
-keep class android.content.res.XmlResourceParser { *; }

# If using specific XML libraries, you might need to keep them
-keep class com.example.xml.** { *; }

# Prevent obfuscation of classes with XmlPullParser
-keepnames class * implements org.xmlpull.v1.XmlPullParser

# If using JAXB
-keep class javax.xml.bind.** { *; }
-dontwarn javax.xml.bind.**

# General XML parsing related keeps
-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}

# If using a specific XML parsing library (e.g., Simple XML), you might need:
-keep public class org.simpleframework.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class org.simpleframework.xml.core.** { *; }
-keep class org.simpleframework.xml.util.** { *; }

# Ensure that required methods are not stripped out
-keepclassmembers class * {
    public <init>(org.xmlpull.v1.XmlPullParser);
}

# Config for R8 in particular
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions