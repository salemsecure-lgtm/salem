# Keep kotlinx.serialization generated serializers
-keepclassmembers class **$$serializer { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.salem.worldcup2026.data.model.** { *; }
