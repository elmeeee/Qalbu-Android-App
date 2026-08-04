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

# WorkManager + Room — bulletproof rules to prevent reflection failures
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keep class androidx.work.impl.WorkDatabase { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class **_Impl { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>(...);
    public <init>();
}
-keepclassmembers @androidx.room.Database class * {
    public abstract <methods>;
}

# AndroidX Startup — keep initializers from being stripped
-keep class * extends androidx.startup.Initializer { *; }
-keepclassmembers class * extends androidx.startup.Initializer {
    <init>();
}

# Glance (widgets) — keep RemoteViewsService & factory classes
-keep class * extends android.widget.RemoteViewsService { *; }
-keep class * extends androidx.glance.** { *; }

# Keep app classes and Hilt-generated wiring intact for release builds.
-keep class app.kamy.saatApp.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# kotlinx.serialization — keep library + generated $$serializer classes
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class app.kamy.saatApp.** {
    *** Companion;
}
-keepclasseswithmembers class app.kamy.saatApp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class app.kamy.saatApp.**$$serializer { *; }

# Retrofit — keep service interfaces + method parameter annotations
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp platform-specific (needed for Android 10+ connection Conscrypt)
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
