package org.mosc.lang;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.mosc.lang.customs.SizeT;
import org.mosc.lang.env.JavaWrapper;

public interface IMosc extends Library {

    int MSCGetVersionNumber();
    Pointer newVM(MSCConfig.ByReference config, JavaWrapper.Config jvmConfig);
    void freeVM(Pointer vm);
    MSCConfig.VMReturnCode interpret(Pointer vm, String module, String source);
    void MSCInitConfig(MSCConfig.ByReference config);
    void MSCVMSetConfig(MSCConfig.ByReference config);
    void setJVMModuleResolver(Pointer vm, MSCConfig.MSCLoadModuleFn resolver);
    void MSCCollectGarbage(MVM mvm);

    MSCConfig.MSCInterpretResult MSCInterpret(Pointer mvm, String module, String source);

    Pointer MSCMakeCallHandle(Pointer vm, String signature);

    MSCConfig.MSCInterpretResult MSCCall(Pointer vm, Pointer method);

    void MSCReleaseHandle(Pointer vm, Pointer handle);


    // Returns the number of slots available to the current foreign method.
    int MSCGetSlotCount(Pointer vm);

    // Ensures that the foreign method stack has at least [numSlots] available for
    // use, growing the stack if needed.
    //
    // Does not shrink the stack if it has more than enough slots.
    //
    // It is an error to call this from a finalizer.
    void MSCEnsureSlots(Pointer vm, int numSlots);

    // Gets the type of the object in [slot].
    MSCConfig.MSCType MSCGetSlotType(Pointer vm, int slot);

    // Reads a boolean value from [slot].
    //
    // It is an error to call this if the slot does not contain a boolean value.
    boolean MSCGetSlotBool(Pointer vm, int slot);

    // Reads a byte array from [slot].
    //
    // The memory for the returned string is owned by org.mosc.lang.Mosc. You can inspect it
    // while in your foreign method, but cannot keep a pointer to it after the
    // function returns, since the garbage collector may reclaim it.
    //
    // Returns a pointer to the first byte of the array and fill [length] with the
    // number of bytes in the array.
    //
    // It is an error to call this if the slot does not contain a string.
    String MSCGetSlotBytes(Pointer vm, int slot, IntByReference length);

    // Reads a number from [slot].
    //
    // It is an error to call this if the slot does not contain a number.
    double MSCGetSlotDouble(Pointer vm, int slot);

    // Reads a foreign object from [slot] and returns a pointer to the foreign data
    // stored with it.
    //
    // It is an error to call this if the slot does not contain an instance of a
    // foreign class.
    Pointer MSCGetSlotExtern(Pointer vm, int slot);

    // Reads a string from [slot].
    //
    // The memory for the returned string is owned by org.mosc.lang.Mosc. You can inspect it
    // while in your foreign method, but cannot keep a pointer to it after the
    // function returns, since the garbage collector may reclaim it.
    //
    // It is an error to call this if the slot does not contain a string.
    String MSCGetSlotString(Pointer vm, int slot);

    // Creates a handle for the value stored in [slot].
    //
    // This will prevent the object that is referred to from being garbage collected
    // until the handle is released by calling [MSCReleaseHandle()].
    Pointer MSCGetSlotHandle(Pointer vm, int slot);

    // Stores the boolean [value] in [slot].
    void MSCSetSlotBool(Pointer vm, int slot, boolean value);

    // Stores the array [length] of [bytes] in [slot].
    //
    // The bytes are copied to a new string within org.mosc.lang.Mosc's heap, so you can free
    // memory used by them after this is called.
    void MSCSetSlotBytes(Pointer vm, int slot, String bytes, SizeT length);

    // Stores the numeric [value] in [slot].
    void MSCSetSlotDouble(Pointer vm, int slot, double value);

    // Creates a new instance of the foreign class stored in [classSlot] with [size]
    // bytes of raw storage and places the resulting object in [slot].
    //
    // This does not invoke the foreign class's constructor on the new instance. If
    // you need that to happen, call the constructor from org.mosc.lang.Mosc, which will then
    // call the allocator foreign method. In there, call this to create the object
    // and then the constructor will be invoked when the allocator returns.
    //
    // Returns a pointer to the foreign object's data.
    Pointer MSCSetSlotNewExtern(Pointer vm, int slot, int classSlot, SizeT size);

    // Stores a new empty list in [slot].
    void MSCSetSlotNewList(Pointer vm, int slot);

    // Stores a new empty map in [slot].
    void MSCSetSlotNewMap(Pointer vm, int slot);

    // Stores null in [slot].
    void MSCSetSlotNull(Pointer vm, int slot);

    // Stores the string [text] in [slot].
    //
    // The [text] is copied to a new string within org.mosc.lang.Mosc's heap, so you can free
    // memory used by it after this is called. The length is calculated using
    // [strlen()]. If the string may contain any null bytes in the middle, then you
    // should use [MSCSetSlotBytes()] instead.
    void MSCSetSlotString(Pointer vm, int slot, String text);


    // Stores the value captured in [handle] in [slot].
    //
    // This does not release the handle for the value.
    void MSCSetSlotHandle(Pointer vm, int slot, Pointer handle);

    // Returns the number of elements in the list stored in [slot].
    int MSCGetListCount(Pointer vm, int slot);

    // Reads element [index] from the list in [listSlot] and stores it in
    // [elementSlot].
    void MSCGetListElement(Pointer vm, int listSlot, int index, int elementSlot);

    // Sets the value stored at [index] in the list at [listSlot],
    // to the value from [elementSlot].
    void MSCSetListElement(Pointer vm, int listSlot, int index, int elementSlot);


    // Takes the value stored at [elementSlot] and inserts it into the list stored
    // at [listSlot] at [index].
    //
    // As in org.mosc.lang.Mosc, negative indexes can be used to insert from the end. To append
    // an element, use `-1` for the index.
    void MSCInsertInList(Pointer vm, int listSlot, int index, int elementSlot);


    // Returns the number of entries in the map stored in [slot].
    int MSCMapCount(Pointer vm, int slot);

    // Returns true if the key in [keySlot] is found in the map placed in [mapSlot].
    boolean MSCMapContainsKey(Pointer vm, int mapSlot, int keySlot);

    // Retrieves a value with the key in [keySlot] from the map in [mapSlot] and
    // stores it in [valueSlot].
    void MSCGetMapValue(Pointer vm, int mapSlot, int keySlot, int valueSlot);

    // Takes the value stored at [valueSlot] and inserts it into the map stored
    // at [mapSlot] with key [keySlot].
    void MSCSetMapValue(Pointer vm, int mapSlot, int keySlot, int valueSlot);

    // Removes a value from the map in [mapSlot], with the key from [keySlot],
    // and place it in [removedValueSlot]. If not found, [removedValueSlot] is
    // set to null, the same behaviour as the org.mosc.lang.Mosc Map API.
    void MSCRemoveMapValue(Pointer vm, int mapSlot, int keySlot,
                           int removedValueSlot);

    // Looks up the top level variable with [name] in resolved [module] and stores
    // it in [slot].
    void MSCGetVariable(Pointer vm, String module, String name,
                        int slot);

    // Looks up the top level variable with [name] in resolved [module],
    // returns false if not found. The module must be imported at the time,
    // use MSCHasModule to ensure that before calling.
    boolean MSCHasVariable(Pointer vm, String module, String name);

    // Returns true if [module] has been imported/resolved before, false if not.
    boolean MSCHasModule(Pointer vm, String module);

    // Sets the current djuru to be aborted, and uses the value in [slot] as the
    // runtime error object.
    void MSCAbortDjuru(Pointer vm, int slot);

    // Returns the user data associated with the org.mosc.lang.MVM.
    Pointer MSCGetUserData(Pointer vm);

    // Sets user data associated with the org.mosc.lang.MVM.
    void MSCSetUserData(Pointer vm, Pointer userData);

}
