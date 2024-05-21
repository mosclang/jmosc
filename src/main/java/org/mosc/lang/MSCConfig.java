package org.mosc.lang;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.mosc.lang.customs.JnaEnum;
import org.mosc.lang.customs.SizeT;

@Structure.FieldOrder({
        "reallocateFn",
        "resolveModuleFn",
        "loadModuleFn",
        "bindExternMethodFn",
        "bindExternClassFn",
        "errorHandler",
        "writeFn",
        "minHeapSize",
        "initialHeapSize",
        "heapGrowthPercent",
        "userData"
})
public class MSCConfig extends Structure {

    @Structure.FieldOrder({"handle"})
    public static class JVMClass extends Structure implements Structure.ByReference {
        public JVMClass(Pointer pointer) {
            super(pointer);
            read();
        }
        public Pointer handle = null;
    }
    @Structure.FieldOrder({"source", "onComplete", "userData"})
    public static class MSCLoadModuleResult extends Structure {
        public String source;
        public MSCLoadModuleCompleteFn onComplete;
        public Pointer userData;

        public interface MSCLoadModuleCompleteFn extends Callback {
            Pointer invoke(Pointer mvm, String name, MSCLoadModuleResult result);
        }
    }

    @Structure.FieldOrder({"allocate", "finalize"})
    public static class MSCExternClassMethods extends Structure  {
        // The callback invoked when the foreign object is created.
        //
        // This must be provided. Inside the body of this, it must call
        // [setSlotNewForeign()] exactly once.
        public MSCExternMethodFn allocate;

        // The callback invoked when the garbage collector is about to collect a
        // foreign object's memory.
        //
        // This may be `NULL` if the foreign class does not need to finalize.
        public MSCFinalizerFn finalize;
        public  static class ByReference extends MSCExternClassMethods implements Structure.ByReference  {}
    }

    public interface MSCReallocator extends Callback {
        Pointer invoke(Pointer memory, SizeT newSize, Pointer userData);
    }

    public interface MSCResolveModuleFn extends Callback {
        String invoke(Pointer mvm, String importer, String name);
    }

    public interface MSCLoadModuleFn extends Callback {
        MSCLoadModuleResult invoke(Pointer mvm, String name);
    }

    public interface MSCExternMethodFn extends Callback {
        void invoke(Pointer djuru);
    }

    public interface MSCFinalizerFn extends MSCExternMethodFn {
    }

    public interface MSCBindExternMethodFn extends Callback {
        MSCExternMethodFn invoke(Pointer mvm, String module, String className, boolean isStatic,
                                 String signature);
    }

    public interface MSCBindExternClassFn extends Callback {
        MSCExternClassMethods invoke(
                Pointer mvm, String module, String className);
    }

    public enum MSCError implements JnaEnum<MSCError> {
        ERROR_COMPILE,
        ERROR_RUNTIME,
        ERROR_STACK_TRACE;

        private static int start = 0;

        public int getIntValue() {
            return this.ordinal() + start;
        }

        public MSCError getForValue(int i) {
            for (MSCError o : MSCError.values()) {
                if (o.getIntValue() == i) {
                    return o;
                }
            }
            return null;
        }
    }

    public enum MSCInterpretResult implements JnaEnum<MSCInterpretResult> {
        RESULT_COMPILATION_ERROR,
        RESULT_RUNTIME_ERROR,
        RESULT_SUCCESS;
        private static int start = 0;

        public int getIntValue() {
            return this.ordinal() + start;
        }

        public MSCInterpretResult getForValue(int i) {
            for (MSCInterpretResult o : MSCInterpretResult.values()) {
                if (o.getIntValue() == i) {
                    return o;
                }
            }
            return null;
        }
    }
    public enum VMReturnCode implements JnaEnum<VMReturnCode> {
        VM_SUCCESS,
        VM_ERROR;
        private static int start = 0;

        public int getIntValue() {
            return this.ordinal() + start;
        }

        public VMReturnCode getForValue(int i) {
            for (VMReturnCode o : VMReturnCode.values()) {
                if (o.getIntValue() == i) {
                    return o;
                }
            }
            return null;
        }
    }

    public enum MSCType implements JnaEnum<MSCType> {
        MSC_TYPE_BOOL,
        MSC_TYPE_NUM,
        MSC_TYPE_EXTERN,
        MSC_TYPE_LIST,
        MSC_TYPE_MAP,
        MSC_TYPE_NULL,
        MSC_TYPE_STRING,

        MSC_TYPE_UNKNOWN;
        private static int start = 0;

        public int getIntValue() {
            return this.ordinal() + start;
        }

        public MSCType getForValue(int i) {
            for (MSCType o : MSCType.values()) {
                if (o.getIntValue() == i) {
                    return o;
                }
            }
            return null;
        }
    }

    public interface MSCErrorHandler extends Callback {
        boolean invoke(Pointer mvm, int type, String moduleName, int line, String message);
    }

    public interface MSCWriteFn extends Callback {
        void invoke(Pointer mvm, String text);
    }

    public static class ByReference extends MSCConfig implements Structure.ByReference {
    }

    public MSCConfig.ByReference asRef() {
        if (this instanceof MSCConfig.ByReference) {
            return (MSCConfig.ByReference) this;
        }
        throw new ClassCastException("Can't cast Config to Config.ByReference");
    }

    @Override
    public String toString() {
        return "MSCConfig{" +
                "reallocateFn=" + reallocateFn +
                ", resolveModuleFn=" + resolveModuleFn +
                ", loadModuleFn=" + loadModuleFn +
                ", bindExternMethodFn=" + bindExternMethodFn +
                ", bindExternClassFn=" + bindExternClassFn +
                ", errorHandler=" + errorHandler +
                ", writeFn=" + writeFn +
                ", minHeapSize=" + minHeapSize +
                ", initialHeapSize=" + initialHeapSize +
                ", heapGrowthPercent=" + heapGrowthPercent +
                ", userData=" + userData +
                '}';
    }

    public MSCReallocator reallocateFn;
    public MSCResolveModuleFn resolveModuleFn;
    public MSCLoadModuleFn loadModuleFn;
    public MSCBindExternMethodFn bindExternMethodFn;
    public MSCBindExternClassFn bindExternClassFn;
    public MSCErrorHandler errorHandler;
    // The callback to use to display text when `System.print()` or the other
    // related functions are called.
    //
    // If this is `NULL`, org.mosc.lang.Mosc discards any printed text.
    public MSCWriteFn writeFn;

    // If zero, defaults to 1MB.
    public SizeT minHeapSize;
    // If zero, defaults to 10MB.
    public SizeT initialHeapSize;
    public int heapGrowthPercent;
    public Pointer userData;
}
