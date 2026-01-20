# Koin
-keepclassmembers class * { public <init>(...); }
-keep class org.koin.** { *; }

# Kotlinx serialization (if used later)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
