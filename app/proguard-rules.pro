# Add project specific ProGuard rules here.

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep Kotlin coroutines
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *** INSTANCE;
}
-keep,includedescriptorclasses class com.example.shiftcalculator.model.**$$serializer { *; }
-keepclassmembers class com.example.shiftcalculator.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.shiftcalculator.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Keep application classes
-keep class com.example.shiftcalculator.** { *; }
-keepclassmembers class com.example.shiftcalculator.** {
    *;
}

# Keep MainActivity
-keep class com.example.shiftcalculator.MainActivity { *; }
