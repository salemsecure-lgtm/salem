# Keep protobuf generated classes
-keep class com.salem.tclremote.polo.** { *; }
-keep class com.salem.tclremote.remote.** { *; }
-keep class com.google.protobuf.** { *; }

# BouncyCastle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**
