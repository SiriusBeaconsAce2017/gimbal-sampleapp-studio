# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
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
-keep public interface com.gimbal.proguard.Keep {  }
-keep public class ** implements com.gimbal.proguard.Keep { *; }
-keep public class **.protocol.** { public protected *; }
-keepattributes Signature

-keep public class org.springframework.** { public protected *; }
-keep public class org.codehaus.jackson..** { public protected *; }
-dontwarn org.codehaus.**,com.fasterxml.**,org.simpleframework.**,com.google.**,org.apache.**