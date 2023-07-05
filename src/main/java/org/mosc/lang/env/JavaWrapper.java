package org.mosc.lang.env;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.mosc.lang.MSCConfig;
import org.mosc.lang.Mosc;
import org.mosc.lang.helpers.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaWrapper {

    private final List<PackageRegistry> packages = new ArrayList<>();
    public Channel channel = new Channel(new HashMap<>());
    public JWrapperModuleResolver loadModule = (name) -> {
        MSCConfig.MSCLoadModuleResult result = new MSCConfig.MSCLoadModuleResult();
        // System.out.println("Loading::>>" + name + ':' + packages.size());
        ModuleRegistry module = findModule(name);
        if (module == null) {
            // System.out.println("module:: null");
            return null;
        }
        result.source = module.source;
        // System.out.println("::" + result.source);
        return result.source;
        // System.out.println(vm + " call: " + name + "---" + "ext");
        //return null;
    };

    public JWrapperReporter reporter = (vm, key, dataType) -> {
        int valueSlot = 2;
        if (dataType == 1) {
            // map data type
            this.channel.data.put(key, JSON.toMap(Mosc.INTERFACE.MSCGetSlotString(vm, valueSlot)));
            return;
        }
        if (dataType == 2) {
            // List data type
            this.channel.data.put(key, JSON.toList(Mosc.INTERFACE.MSCGetSlotString(vm, valueSlot)));
            return;
        }
        MSCConfig.MSCType type = Mosc.INTERFACE.MSCGetSlotType(vm, valueSlot);
        switch (type) {
            case MSC_TYPE_NUM:
                this.channel.data.put(key, Mosc.INTERFACE.MSCGetSlotDouble(vm, valueSlot));
                break;
            case MSC_TYPE_BOOL:
                this.channel.data.put(key, Mosc.INTERFACE.MSCGetSlotBool(vm, valueSlot));
                break;
            case MSC_TYPE_NULL:
                this.channel.data.put(key, null);
                break;
            case MSC_TYPE_EXTERN:
                this.channel.data.put(key, Mosc.INTERFACE.MSCGetSlotExtern(vm, valueSlot));
                break;
            case MSC_TYPE_STRING:
                this.channel.data.put(key, Mosc.INTERFACE.MSCGetSlotString(vm, valueSlot));
                break;
            case MSC_TYPE_UNKNOWN:
                break;
        }

    };

    public void registerPackage(PackageRegistry packageRegistry) {
        this.packages.add(packageRegistry);
    }

    public static class Channel {
        public Map<String, Object> data;

        public Channel(Map<String, Object> data) {
            this.data = data;
        }
    }

    private ModuleRegistry findModule(String name) {
        for (PackageRegistry packageRegistry : packages) {
            for (ModuleRegistry moduleRegistry : packageRegistry.modules) {
                if (moduleRegistry.name.equals(name)) {
                    return moduleRegistry;
                }
            }
        }
        return null;
    }


    public MSCConfig.MSCExternMethodFn resolveExternMethod(Pointer vm, String moduleName, String className, boolean isStatic,
                                                           String signature) {
        ModuleRegistry module = findModule(moduleName);
        if (module == null) {
            return null;
        }
        ClassRegistry clazz = module.findClass(className);
        if (clazz == null) return null;
        MethodRegistry methodRegistry = clazz.findMethod(isStatic, signature);
        if (methodRegistry == null) return null;
        return methodRegistry.getMethod();
    }

    public MSCConfig.MSCExternClassMethods resolveExternClass(Pointer vm, String moduleName, String className) {
        MSCConfig.MSCExternClassMethods.ByReference methods = new MSCConfig.MSCExternClassMethods.ByReference();
        // System.out.println("Allocated:: " + methods.allocate + "))" + methods.finalize);
        // Mosc.INTERFACE.MSCInitExternClassMethods(methods);
        ModuleRegistry module = findModule(moduleName);
        if (module == null) {
            return null;
        }
        ClassRegistry clazz = module.findClass(className);
        if (clazz == null) return null;
        // System.out.println("resolveExternClass:::" + moduleName + "," + className + ')' + module + '-' + clazz + ')' + methods.allocate);

        methods.finalize = (MSCConfig.MSCFinalizerFn) clazz.findMethodFn(true, "<finalize>");
        methods.allocate = clazz.findMethodFn(true, "<allocate>");
        methods.write();
        return methods;
    }

    public interface JWrapperReporter extends Callback {
        void invoke(Pointer vm, String key, int type);
    }

    public interface JWrapperModuleResolver extends Callback {
        String invoke(String name);
    }

    @Structure.FieldOrder({
            "hostExternClassLoader",
            "hostExternMethodLoader",
            "hostLoadModuleLoader",
            "reporter",
    })
    public static class Config extends Structure {
        public MSCConfig.MSCBindExternClassFn hostExternClassLoader;
        public MSCConfig.MSCBindExternMethodFn hostExternMethodLoader;
        public JWrapperModuleResolver hostLoadModuleLoader;
        public JWrapperReporter reporter;

        public static class ByReference extends Config implements Structure.ByReference {
        }
    }

    public interface BindExternClassFn extends Callback {
        MSCConfig.MSCExternClassMethods invoke(
                Pointer mvm, String module, String className);
    }


    public static class ClassBuilder {
        private final ClassRegistry registry;
        private final ModuleBuilder parent;

        public ClassBuilder(ModuleBuilder parent, String name) {
            this.registry = new ClassRegistry(name, new ArrayList<>());
            this.parent = parent;
        }

        public ClassBuilder method(boolean isStatic, String signature, MSCConfig.MSCExternMethodFn fn) {
            this.registry.methods.add(new MethodRegistry(isStatic, signature, fn));
            return this;
        }

        private ClassRegistry build() {
            return registry;
        }

        public ModuleBuilder end() {
            parent.collect(build());
            return parent;
        }
    }

    public static class ModuleBuilder {
        private final ModuleRegistry registry;
        private PackageBuilder parent;

        public ModuleBuilder(PackageBuilder parent, String name, String source) {
            this.registry = new ModuleRegistry(name, source, new ArrayList<>());
            this.parent = parent;
        }

        public ClassBuilder clazz(String name) {
            return new ClassBuilder(this, name);
        }

        private ModuleRegistry build() {
            return registry;
        }

        public PackageBuilder end() {
            parent.collect(this.build());
            return parent;
        }

        void collect(ClassRegistry moduleRegistry) {
            registry.classes.add(moduleRegistry);
        }
    }

    // new PackageBuilder("java").module("java").clazz("JLM").method().method().end().end()
    public static class PackageBuilder {
        private final PackageRegistry registry;

        public PackageBuilder(String name) {
            registry = new PackageRegistry(name, new ArrayList<>());
        }

        public ModuleBuilder module(String name, String source) {
            return new ModuleBuilder(this, name, source);
        }

        public PackageRegistry build() {
            return registry;
        }

        void collect(ModuleRegistry moduleRegistry) {
            registry.modules.add(moduleRegistry);
        }
    }

    public static class PackageRegistry {
        private String name;
        private List<ModuleRegistry> modules;

        public PackageRegistry() {
        }

        public PackageRegistry(String name, List<ModuleRegistry> modules) {
            this.name = name;
            this.modules = modules;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<ModuleRegistry> getModules() {
            return modules;
        }

        public void setModules(List<ModuleRegistry> modules) {
            this.modules = modules;
        }

    }

    public static class ModuleRegistry {
        private String name;
        private String source;
        private List<ClassRegistry> classes;

        public ModuleRegistry() {
        }

        public ModuleRegistry(String name, String source, List<ClassRegistry> classes) {
            this.name = name;
            this.source = source;
            this.classes = classes;
        }

        public ClassRegistry findClass(String name) {
            for (ClassRegistry classRegistry : classes) {
                if (classRegistry.name.equals(name)) return classRegistry;
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public List<ClassRegistry> getClasses() {
            return classes;
        }

        public void setClasses(List<ClassRegistry> classes) {
            this.classes = classes;
        }
    }

    public static class ClassRegistry {
        private String name;
        private List<MethodRegistry> methods;

        public MSCConfig.MSCExternMethodFn findMethodFn(boolean isStatic, String signature) {
            // System.out.println("Findind method " + signature);
            MethodRegistry found = findMethod(isStatic, signature);
            if (found == null) return null;
            return found.getMethod();
        }

        public MethodRegistry findMethod(boolean isStatic, String signature) {
            // System.out.println("Findind method " + signature);
            for (MethodRegistry methodRegistry : methods) {
                if (isStatic == methodRegistry.isStatic && methodRegistry.signature.equals(signature))
                    return methodRegistry;
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<MethodRegistry> getMethods() {
            return methods;
        }

        public void setMethods(List<MethodRegistry> methods) {
            this.methods = methods;
        }

        public ClassRegistry() {
        }

        public ClassRegistry(String name, List<MethodRegistry> methods) {

            this.name = name;
            this.methods = methods;
        }
    }

    public static class MethodRegistry {
        private boolean isStatic;
        private String signature;
        private MSCConfig.MSCExternMethodFn method;

        public MethodRegistry() {
        }

        public MethodRegistry(boolean isStatic, String signature, MSCConfig.MSCExternMethodFn method) {
            this.isStatic = isStatic;
            this.signature = signature;
            this.method = method;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public void setStatic(boolean aStatic) {
            isStatic = aStatic;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public MSCConfig.MSCExternMethodFn getMethod() {
            return method;
        }

        public void setMethod(MSCConfig.MSCExternMethodFn method) {
            this.method = method;
        }
    }
}
