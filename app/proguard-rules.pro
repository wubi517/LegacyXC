-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontobfuscate
-dontpreverify
-dontwarn **
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

# Do not optimize/shrink LibVLC, because of native code
-keep class org.videolan.libvlc.** { *; }
# Same for MediaLibrary
-keep class org.videolan.medialibrary.** { *; }

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep class org.json.* { *; }
# Samsung shit workaround
# see https://code.google.com/p/android/issues/detail?name=78377
# see also: https://code.google.com/p/android/issues/detail?name=78377#c322
-keepattributes **
-keep class !android.support.v7.view.menu.**,!android.support.design.internal.NavigationMenu,!android.support.design.internal.NavigationMenuPresenter,!android.support.design.internal.NavigationSubMenu, android.support.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

-dontwarn android.support.**

# The Android pre-handler for exceptions is loaded reflectively (via ServiceLoader).
-keep class kotlinx.coroutines.experimental.android.AndroidExceptionPreHandler { *; }

-dontwarn com.squareup.okhttp.**

-keep class org.infradead.libopenconnect** { *; }
-keep class org.stoken.** { *; }
-dontwarn org.acra.**
-dontwarn android.app.**
-dontwarn android.content.res.**
-dontwarn de.robv.android.xposed.**
-dontwarn de.robv.android.xposed.callbacks**
-ignorewarnings
-keep class * {
    public private *;
}
-keep class com.newlegacyxc.** { *; }
-keep class org.acra.** { *; }
-keep class android.app.** { *; }
-keep class android.content.res.** { *; }
-keep class de.robv.android.xposed.** { *; }
-keep class de.robv.android.xposed.callbacks** { *; }

-keep class tv.danmaku.ijk.media.player.** { * ; }
-dontwarn tv.danmaku.ijk.media.player.*
-keep interface tv.danmaku.ijk.media.player.** { *; }
-keep class tv.danmaku.ijk.media.player.IjkMediaPlayer { *; }
-keep class tv.danmaku.ijk.media.player.ffmpeg.FFmpegApi { *; }

-keep class com.google.android.exoplayer.** {*;}
