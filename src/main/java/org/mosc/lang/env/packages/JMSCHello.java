package org.mosc.lang.env.packages;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import org.mosc.lang.MSCConfig;
import org.mosc.lang.MSCRuntime;
import org.mosc.lang.Mosc;
import org.mosc.lang.env.JavaWrapper;

import java.util.HashMap;
import java.util.Map;

public class JMSCHello {
    public static void initializer(Pointer mvm) {
        // System.out.println("Allocating DHello");
        Mosc.INTERFACE.MSCSetSlotNewJVMClass(mvm);
        // Mosc.INTERFACE.MSCSetSlotNewExtern(mvm, 0, 0, 16);
        // ref.writeField("handle");
    }

    ;

    public static JavaWrapper.PackageRegistry registry(MSCRuntime runtime) {
        return new JavaWrapper.PackageBuilder("jstuff")
                .module("jhello", "kulu JHello {\n" +
                        "    dunan dialen helloJava()\n" +
                        "}\n" +
                        "\n" +
                        "dunan kulu DIHello {\n" +
                        "    dilan kura() {\n" +
                        "    ale._open()" +
                        "\n" +
                        "    }\n" +
                        "dunan _open()" +
                        "}")
                .clazz("JHello")
                .method(true, "helloJava()", mvm -> System.out.println("Hello From Java"))
                .end()
                .clazz("DIHello")
                .method(true, "<allocate>", mvm -> {
                    // System.out.println("Allocating DIHello");
                    // Mosc.INTERFACE.MSCSetSlotNewJVMClass(mvm);
                    Mosc.INTERFACE.MSCSetSlotNewExtern(mvm, 0, 0, Mosc.sizeOf(MSCConfig.JVMClass.class));

                })
                .method(true, "<finalize>", (MSCConfig.MSCFinalizerFn) mvm -> {
                    System.out.println("Finalizing::::");
                })
                .method(false, "_open()", mvm -> {
                    Pointer pointer = Mosc.INTERFACE.MSCGetSlotExtern(mvm, 0);
                    MSCConfig.JVMClass foreign = new MSCConfig.JVMClass(pointer);
                    DIHElement diel = new DIHElement();
                    diel.test = "Hello";
                    diel.write();
                    foreign.handle = diel.getPointer();
                    foreign.write();
                    System.out.println("foreign:: " + foreign.handle + ')' + diel.test);
                    diel = new DIHElement(foreign.handle);
                    System.out.println("foreign:: " + foreign.handle + ')' + diel.test);
                })
                .end()
                .end()
                .build();
    }

    @Structure.FieldOrder({"test"})
    public static class DIHElement extends Structure implements Structure.ByReference {
        public DIHElement() {
        }

        public DIHElement(Pointer p) {
            super(p);
            read();
        }

        public String test = "Molo";
        // public Map<String, String> map = new HashMap<>();
    }
}
