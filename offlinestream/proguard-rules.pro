
-printconfiguration '/home/fanap_soft_26/Development/proguard-configuration.txt'
-dontwarn kotlinx.atomicfu.**
#
#-keep class ir.fanap.podstream.offlineStream.PodStream{*;}
#-keepclassmembers class ir.fanap.podstream.offlineStream.PodStream{*;}
#
#-keep class * implements ir.fanap.podstream.network.response.AvoidObfuscate{*;}
#-keepclassmembers  class * implements ir.fanap.podstream.network.response.AvoidObfuscate$* { *; }
#
#-keep class * implements com.example.kafkassl.kafkaclient.ConsumResult{*;}
#-keepclassmembers  class com.example.kafkassl.kafkaclient.ConsumResult { *; }
#
#-keep class * implements com.example.kafkassl.kafkaclient.ConsumerClient{*;}
#-keepclassmembers  class com.example.kafkassl.kafkaclient.ConsumerClient { *; }
#
#-keep class * implements com.example.kafkassl.kafkaclient.ProducerClient{*;}
#-keepclassmembers  class com.example.kafkassl.kafkaclient.ProducerClient { *; }
#
#
#-keep class  com.google.android.exoplayer2.C{*;}
#-keepclassmembers  class com.google.android.exoplayer2.C { *; }
#
#-keep class  com.google.android.exoplayer2.upstream.BaseDataSource{*;}
#-keepclassmembers  class com.google.android.exoplayer2.upstream.BaseDataSource { *; }
#
#-keep class  com.google.android.exoplayer2.upstream.DataSource{*;}
#-keepclassmembers  class com.google.android.exoplayer2.upstream.DataSource { *; }
#
#-keep class   com.google.android.exoplayer2.upstream.TransferListener{*;}
#-keepclassmembers ,allowshrinking class  com.google.android.exoplayer2.upstream.TransferListener { *; }
#
#-keep class   com.google.android.exoplayer2.upstream.DataSpec{*;}
#-keepclassmembers  class com.google.android.exoplayer2.upstream.DataSpec { *; }
#
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}
#

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keepattributes EnclosingMethod
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.

# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature