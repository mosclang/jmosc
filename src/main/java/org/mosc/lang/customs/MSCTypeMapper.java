package org.mosc.lang.customs;


import com.sun.jna.DefaultTypeMapper;

public class MSCTypeMapper extends DefaultTypeMapper {

    public MSCTypeMapper() {
        // The EnumConverter is set to fire when instances of
        // our interface, JnaEnum, are seen.
        addTypeConverter(JnaEnum.class, new EnumConverter());
    }
}