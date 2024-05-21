package org.mosc.lang;


import com.sun.jna.Pointer;

public class MVM {
    public Pointer underlined;
    /**
     * Create from native pointer.  Don't use this unless you know what
     * you're doing.
     *
     * @param vmPointer
     */
    public MVM(Pointer vmPointer) {
        this.underlined = vmPointer;
    }


    public static class MSCHandle {
        Pointer underlined;

        /**
         * Create from native pointer.  Don't use this unless you know what
         * you're doing.
         *
         * @param peer
         */
        public MSCHandle(Pointer peer) {
            this.underlined = peer;
        }

    }
}
