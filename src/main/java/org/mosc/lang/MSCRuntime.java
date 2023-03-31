package org.mosc.lang;

import org.mosc.lang.env.JavaWrapper;

import java.util.HashMap;
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

    public MSCConfig.VMReturnCode run(String module, String code, Map<String, String> input) {
        String fullSource = buildInput(input) + code;
        return this.moscInterface.interpret(this.mvm.underlined, module, fullSource);
    }

    private String buildInput(Map<String, String> params) {
        System.out.println("buildInput::: " + params);
        String head = "tii _(map) {\nJWrapper.report(map)\n}\n" +
                "tii __(key, value) {\nJWrapper.report(key, value)\n}\n";
        if (params.isEmpty()) return "kabo \"java\" nani JWrapper\nnin _JINPUT_ = {}\n" + head + "\n";
        StringBuilder ret = new StringBuilder("kabo \"java\" nani JWrapper\nnin _JINPUT_ = {");
        params.forEach((k, v) -> ret.append(k).append(":").append(v).append(","));
        return ret.toString().replaceAll(",$", "}\n") + "\n" + head + "\n";
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
