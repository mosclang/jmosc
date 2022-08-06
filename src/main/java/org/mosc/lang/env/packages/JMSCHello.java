package org.mosc.lang.env.packages;

import org.mosc.lang.env.JavaWrapper;

public class JMSCHello {
    public static JavaWrapper.PackageRegistry registry() {
        return new JavaWrapper.PackageBuilder("jstuff")
                .module("jhello", "kulu JHello {\n" +
                        "    dunan dialen helloJava()\n" +
                        "}\n")
                .clazz("JHello")
                .method(true, "helloJava()", mvm -> System.out.println("Hello From Java"))
                .end()
                .end()
                .build();
    }
}
