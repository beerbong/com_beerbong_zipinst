package com.koushikdutta.rommanager.api;

interface IClockworkRecoveryScriptBuilder {
    void backupWithPath(String path);
    void backup();
    void restore(String path, boolean boot, boolean system, boolean data, boolean cache, boolean sdext);
    void installZip(String path);
    void print(String string);
    void runProgram(String program, in List<String> args);
    void format(String path);
    void mount(String path);
    void umount(String path);
    
    void runScript();
}
