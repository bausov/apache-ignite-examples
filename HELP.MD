https://www.gridgain.com/docs/latest/installation-guide/installing-using-docker

# Run latest version
sudo docker run -d gridgain/community
# Run specific GridGain version
sudo docker run -d gridgain/community:8.7.7
# Expose all ports
sudo docker run --network host -d gridgain/community:8.7.7


# Running GridGain with Java 9 or later
https://intellij-support.jetbrains.com/hc/en-us/community/posts/360001797820-How-to-add-add-exports-

--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
###### Used to get access to java UNSAFE (access to offheap memory).

--illegal-access=permit
###### Allows access to private fields without accessors, used for dependency injection in user classes (for example, @IgniteInstanceResource annotation).

--add-exports=java.base/sun.nio.ch=ALL-UNNAMED
###### Used for java.nio Selector optimization.

--add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED
###### Used to enable JMX monitoring capability.

--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED
###### Used to allow jvisualvm to connect to Ignite process.

--add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED
###### Used for fast object construction during marshalling/unmarshalling.

###### COPY:
--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
--illegal-access=permit
--add-exports=java.base/sun.nio.ch=ALL-UNNAMED
--add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED
--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED
--add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED