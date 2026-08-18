# ===================================================================
# StudySync Android - ProGuard & R8 Optimization Rules
# ===================================================================

# -------------------------------------------------------------------
# 1. Kotlinx Serialization (CRITICAL: DTOs & Models)
# -------------------------------------------------------------------
# Keep all classes annotated with @Serializable and their companion serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt

# Keep serializable model classes & their fields from being obfuscated/stripped
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep companion objects that implement KSerializer / generated serializers
-keepclassmembers class * {
    public static final **$serializer INSTANCE;
}

-keepclasseswithmembers class * {
    public static final ** Companion;
}

-keepclassmembers class * extends kotlinx.serialization.KSerializer {
    public static final ** INSTANCE;
}

# Keep all DTOs explicitly in the remote package
-keep class com.example.studysyncandroid.data.remote.dto.** { *; }

# -------------------------------------------------------------------
# 2. Room Database
# -------------------------------------------------------------------
# Keep entity classes and database table names intact
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.example.studysyncandroid.data.local.entities.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *;
}

# -------------------------------------------------------------------
# 3. Ktor Client & OkHttp & CIO
# -------------------------------------------------------------------
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# OkHttp engine & logging
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class okhttp3.internal.publicsuffix.PublicSuffixDatabase {
    java.lang.String[] *;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# -------------------------------------------------------------------
# 4. Hilt / Dagger Dependency Injection
# -------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class com.example.studysyncandroid.di.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * {
    public <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# -------------------------------------------------------------------
# 5. Jetpack Compose & Material 3
# -------------------------------------------------------------------
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.**

# -------------------------------------------------------------------
# 6. Coroutines & DataStore
# -------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**
