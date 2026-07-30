# Add project specific ProGuard rules here.
# By default the flags in this file are applied to the released build.
# You can include rules that should only apply to release builds here.

# If you use the AndroidX Compose library, no specific rules are required here.
# Room, Kotlin Coroutines and most other libraries ship their own consumer rules.

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
