package org.mosc.lang;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.mosc.lang.customs.MSCTypeMapper;
import org.mosc.lang.env.JavaWrapper;
import org.mosc.lang.env.packages.JMSCHello;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Mosc {
    public static IMosc INTERFACE;
    public static List<MSCRuntime> runtimes = new ArrayList<>();

    public static void init(String libName) {
        // System.loadLibrary("mosc");
        Map<String, Object> options = new HashMap<>();
        /*options.put(Library.OPTION_FUNCTION_MAPPER, (FunctionMapper) (library, method) -> {
            String methodName = method.getName();
            return "_" + methodName;
        });*/
        Native.setProtected(true);
        options.put(Library.OPTION_TYPE_MAPPER, new MSCTypeMapper());
        INTERFACE = Native.load(libName, IMosc.class, options);
    }

    private static void ensureLoad() {
        if (INTERFACE == null) {
            throw new RuntimeException("Library not loaded, please call init method first");
        }
    }

    public static MSCRuntime newRuntime(MSCConfig.ByReference config, JavaWrapper.Config.ByReference jmvmConfig) {
        ensureLoad();
        MSCRuntime runtime = new MSCRuntime(INTERFACE, config, jmvmConfig);
        runtimes.add(runtime);
        return runtime;
    }
    public static MSCRuntime newRuntime(MSCConfig.ByReference config) {
        ensureLoad();
        MSCRuntime runtime = new MSCRuntime(INTERFACE, config);
        runtimes.add(runtime);
        return runtime;
    }
    public static void initConfig(MSCConfig.ByReference config) {
        ensureLoad();
        INTERFACE.MSCInitConfig(config);
    }
    public static MSCRuntime newRuntime() {
        ensureLoad();
        MSCRuntime runtime = new MSCRuntime(INTERFACE);
        runtimes.add(runtime);
        return runtime;
    }

    public void shutdownAll() {
        ensureLoad();
        runtimes.forEach(MSCRuntime::shutdown);
    }

    public static String loadResource(String name) {
        try (BufferedReader din = new BufferedReader(new InputStreamReader(Mosc.class.getResourceAsStream("/test/" + name)))) {
            return din.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

     public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        // System.setProperty("java.library.path", "libs/");
        // Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        // fieldSysPath.setAccessible(true);
        // fieldSysPath.set(null, null);
        // System.out.println(System.getProperty("java.library.path"));
        //String source = "kabo \"java\" nani JWrapper\ntii fib(n) {\n" +
        //        "    nii n < 2 segin niin n;\n" +
        //        "    segin niin fib(n - 2) + fib(n - 1);\n" +
        //        "}\n" +
        //        "A.yira(fib(10))\n" +
        //        "JWrapper.status(\"OK\")\n" +
        //        "JWrapper.report(\"test\", \"TValue\")\n" +
        //        "A.yira(A.waati())";
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
}
