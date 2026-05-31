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

-keepclassmembers class com.example.data.remote.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class * extends androidx.room3.RoomDatabase { *; }
-keep class * extends androidx.paging.PagingSource { *; }
-keep class * extends androidx.paging.RemoteMediator { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
