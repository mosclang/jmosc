package org.mosc.lang.env.packages;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import org.mosc.lang.MSCConfig;
import org.mosc.lang.MSCRuntime;
import org.mosc.lang.MVM;
import org.mosc.lang.Mosc;
import org.mosc.lang.env.JavaWrapper;

import java.util.HashMap;
import java.util.Map;

public class JMSCHello {
    public static void initializer(Pointer djuru) {
        // System.out.println("Allocating DHello");
        Mosc.INTERFACE.MSCSetSlotNewJVMClass(djuru);
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
                        "    ale._open()\n" +
                        "    A.yira(\"in\")"+
                        "\n" +
                        "    }\n" +
                        "dunan _open()\n" +
                        "dunan value\n" +
                        "}")
                .clazz("JHello")
                .method(true, "helloJava()", djuru -> System.out.println("Hello From Java"))
                .end()
                .clazz("DIHello")
                .method(true, "<allocate>", djuru -> {
                    // System.out.println("Allocating DIHello");
                    // Mosc.INTERFACE.MSCSetSlotNewJVMClass(mvm);
                    Mosc.INTERFACE.MSCSetSlotNewExtern(djuru, 0, 0, Mosc.sizeOf(MSCConfig.JVMClass.class));

                })
                .method(true, "<finalize>", (MSCConfig.MSCFinalizerFn) mvm -> {
                    System.out.println("Finalizing::::");
                })
                .method(false, "_open()", djuru -> {
                    Pointer pointer = Mosc.INTERFACE.MSCGetSlotExtern(djuru, 0);
                    MSCConfig.JVMClass foreign = new MSCConfig.JVMClass(pointer);
                    DIHElement diel = new DIHElement();
                    diel.test = "Hello Molo";
                    diel.write();
                    foreign.handle = diel.getPointer();
                    foreign.write();
                    // diel = new DIHElement(foreign.handle);
                })
                .method(false, "value", djuru -> {
                    Pointer pointer = Mosc.INTERFACE.MSCGetSlotExtern(djuru, 0);
                    MSCConfig.JVMClass foreign = new MSCConfig.JVMClass(pointer);
                    Mosc.INTERFACE.MSCSetSlotString(djuru, 0, (new DIHElement(foreign.handle)).test);
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
