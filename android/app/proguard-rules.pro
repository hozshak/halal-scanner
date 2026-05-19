# Keep ML Kit Barcode reflection
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# OkHttp + Conscrypt
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
