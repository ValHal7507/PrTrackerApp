-dontwarn javax.annotation.Nullable

# ---- Keep the entire app (Gson + WorkManager + Compose all use reflection) ----
-keep class com.example.prtracker.** { *; }
-keepclassmembers class com.example.prtracker.** { *; }

# ---- Kotlin metadata ----
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations

# ---- Gson generic types ----
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ---- Kotlin coroutines (often stripped by R8) ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---- Debugging: keep line numbers for crash stacks ----
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
