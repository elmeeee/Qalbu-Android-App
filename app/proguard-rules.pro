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

# Keep attributes for R8 optimization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions,LineNumberTable,SourceFile

# Okio & ResourceFileSystem optimization
-dontwarn okio.**
-keep class okio.** { *; }

# Media3 & Foreground Services
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.common.** { *; }

# Keep app classes and Hilt-generated wiring intact for release builds.
-keep class app.kamy.saatApp.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class kotlinx.serialization.** { *; }