# Regles ProGuard/R8 specifiques au projet ClipMemo.
# R8 est actif en release : il reduit et obfusque le binaire. Attention, il
# n'obfusque PAS les chaines litterales (BuildConfig.BACKEND_APP_KEY reste
# lisible dans le dex) ; la protection reelle de l'API passe par une
# attestation d'integrite cote serveur.

# ---------------------------------------------------------------------------
# kotlinx.serialization
# Les serializers sont generes et resolus par reflexion sur les noms de classe.
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Modeles @Serializable du projet et leurs serializers generes.
-keep,includedescriptorclasses class com.danstudios.reelnotes.**$$serializer { *; }
-keepclassmembers class com.danstudios.reelnotes.** {
    *** Companion;
}
-keepclasseswithmembers class com.danstudios.reelnotes.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------------
# Room
# Les entites et DAO sont resolus par le code genere.
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# OkHttp / Okio
# ---------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---------------------------------------------------------------------------
# WebView : l'extraction Instagram evalue du JavaScript et lit des cookies.
# ---------------------------------------------------------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ---------------------------------------------------------------------------
# Coroutines
# ---------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---------------------------------------------------------------------------
# Conserver les numeros de ligne pour des stacktraces exploitables.
# ---------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
