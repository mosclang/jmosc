# **Introduction**
The main purpose of mosc being to be embbed in host application, this wrapper allow a `java` application to embbed mosc 
VM and execute mosc code. A brigde is created between JVM and mosc VM through JNA library upon [jmosc][] 
# **Setup**

To quick test this library, you will need the shared library from [jmosc][] project then put it under the execution folder.

## **Usage**
Firstly you need to init `Mosc` with the dynamic library name, then you instantiate a new `MSCRuntime` from static methode `newRuntime`.


```java
public static void main(String[] args) {
        Mosc.init("javamosc");
        System.out.println("Version::" + Mosc.INTERFACE.MSCGetVersionNumber());
        int count = 1;
        while (count > 0) {
            String source = loadResource("test1.msc");
            count--;
            MSCRuntime runtime = Mosc.newRuntime();
            runtime.registerPackage(JMSCHello.registry());
            int finalCount = count;
            runtime.run("<script>", source, new HashMap<String, String>() {{
                put("imei", "1092020022");
                put("msisdn", "76299780");
                put("count", "\"c" + finalCount + "\"");
            }});

            runtime.shutdown();
            System.out.println("Content::" + runtime.javaWrapper.channel.data);
        }
    }
```

## **Mosc** Interface
`Mosc` class is responsible of native library loading and management, it holds a single instance of `IMosc` (native interface).


[jmosc]: https://github.com/mosclang/jmosc-wrapper