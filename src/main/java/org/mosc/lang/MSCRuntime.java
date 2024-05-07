package org.mosc.lang;

import org.mosc.lang.env.JavaWrapper;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MSCRuntime {
    private final IMosc moscInterface;
    public final JavaWrapper javaWrapper = new JavaWrapper();
    private final MVM mvm;
    private final MSCConfig.ByReference config;
    private final JavaWrapper.Config.ByReference jmvmConfig;

    public MSCRuntime(IMosc moscInterface, MSCConfig.ByReference config) {
        this.moscInterface = moscInterface;
        this.config = config;
        this.jmvmConfig = new JavaWrapper.Config.ByReference();
        this.jmvmConfig.hostLoadModuleLoader = javaWrapper.loadModule;
        this.jmvmConfig.hostExternClassLoader = javaWrapper::resolveExternClass;
        this.jmvmConfig.reporter = this.javaWrapper.reporter;
        this.jmvmConfig.hostExternMethodLoader = javaWrapper::resolveExternMethod;
        this.jmvmConfig.write();
        // this.vm = null;
        this.mvm = new MVM(moscInterface.newVM(config, this.jmvmConfig));
        // this.moscInterface.MSCVMSetConfig(this.config);
    }

    public MSCRuntime(IMosc moscInterface, MSCConfig.ByReference config, JavaWrapper.Config.ByReference jmvmConfig) {
        this.moscInterface = moscInterface;
        this.config = config;
        this.jmvmConfig = jmvmConfig;

        this.mvm = new MVM(moscInterface.newVM(config, this.jmvmConfig));
    }

    public MSCRuntime(IMosc moscInterface) {
        this.moscInterface = moscInterface;
        this.config = new MSCConfig.ByReference();
        this.moscInterface.MSCInitConfig(this.config);
        this.config.writeFn = (mvm, text) -> System.out.print(text);
        this.config.errorHandler = (mvm, type, moduleName, line, message) -> {
            System.out.printf("Error at %s > %d: %s\n", moduleName, line, message);
            return true;
        };
        this.config.write();

        this.jmvmConfig = new JavaWrapper.Config.ByReference();
        this.jmvmConfig.hostLoadModuleLoader = javaWrapper.loadModule;
        this.jmvmConfig.hostExternClassLoader = javaWrapper::resolveExternClass;
        this.jmvmConfig.reporter = this.javaWrapper.reporter;
        this.jmvmConfig.hostExternMethodLoader = javaWrapper::resolveExternMethod;
        this.jmvmConfig.write();
        // this.vm = null;
        this.mvm = new MVM(moscInterface.newVM(config, this.jmvmConfig));
        // this.moscInterface.MSCVMSetConfig(this.config);
    }


    public MSCConfig.VMReturnCode run(String module, String code) {
        return run(module, code, new HashMap<>());
    }

    public MSCConfig.VMReturnCode run(String module, String code, Map<String, Object> input) {
        String fullSource = buildInput(input) + code;
        return this.moscInterface.interpret(this.mvm.underlined, module, fullSource);
    }

    private String buildInput(Map<String, Object> params) {
        System.out.println("buildInput::: " + params);
        String head = "tii _(map) {\nJWrapper.report(map)\n}\n" +
                "tii __(key, value) {\nJWrapper.report(key, value)\n}\n";
        if (params.isEmpty()) return "kabo \"java\" nani JWrapper\nnin _JINPUT_ = {}\n" + head + "\n";
        // params.forEach((k, v) -> ret.append("\"").append(k).append("\"").append(":").append(v).append(","));
        return ("kabo \"java\" nani JWrapper\nnin _JINPUT_ = " + handleInputValue(params)
                // params.forEach((k, v) -> ret.append("\"").append(k).append("\"").append(":").append(v).append(","));
        ).replaceAll(",$", "}\n") + "\n" + head + "\n";
    }
    private String handleInputValue(Object data) {
        if(data == null) {
            return "gansan";
        } else if(data instanceof Map) {
            StringBuilder ret = new StringBuilder("{");
            ((Map<?, ?>) data).forEach((k, v) -> ret.append("\"").append(k).append("\"").append(":").append(handleInputValue(v)).append(","));
            return ret.append("}").toString();
        } else if(data instanceof Collection) {
            StringBuilder ret = new StringBuilder("[");
            ((Collection<?>) data).forEach((d) -> ret.append(handleInputValue(d)).append(","));
            return ret.append("]").toString();
        } else if(data instanceof String) {
            return "\"" + ((String) data).replaceAll("\"", "\\\"") + "\"";
        } else if(data instanceof Number) {
            return data.toString();
        } else if(data instanceof Boolean) {
            return (Boolean) data ? "tien": "galon";
        } else if(data instanceof Object[]) {
            StringBuilder ret = new StringBuilder("[");
            for(Object d: (Object[])data) {
                ret.append(handleInputValue(d)).append(",");
            }
            return ret.append("]").toString();
        }  else if(data instanceof int[]) {
            StringBuilder ret = new StringBuilder("[");
            for(Object d: (int[])data) {
                ret.append(handleInputValue(d)).append(",");
            }
            return ret.append("]").toString();
        }  else if(data instanceof long[]) {
            StringBuilder ret = new StringBuilder("[");
            for(Object d: (long[])data) {
                ret.append(handleInputValue(d)).append(",");
            }
            return ret.append("]").toString();
        } else if(data instanceof double[]) {
            StringBuilder ret = new StringBuilder("[");
            for(Object d: (double[])data) {
                ret.append(handleInputValue(d)).append(",");
            }
            return ret.append("]").toString();
        } else if(data instanceof float[]) {
            StringBuilder ret = new StringBuilder("[");
            for(Object d: (float[])data) {
                ret.append(handleInputValue(d)).append(",");
            }
            return ret.append("]").toString();
        } else if(data instanceof boolean[]) {
            StringBuilder ret = new StringBuilder("[");
            for(Object d: (boolean[])data) {
                ret.append(handleInputValue(d)).append(",");
            }
            return ret.append("]").toString();
        }
        return "gansan";
    }

    public void shutdown() {
        this.moscInterface.freeVM(this.mvm.underlined);
    }

    public void registerPackage(JavaWrapper.PackageRegistry registry) {
        javaWrapper.registerPackage(registry);
    }

    public MVM vm() {
        return this.mvm;
    }

    public Map<String, Object> output() {
        return javaWrapper.channel.data;
    }
}
