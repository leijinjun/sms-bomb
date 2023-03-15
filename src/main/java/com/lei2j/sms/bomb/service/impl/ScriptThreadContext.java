package com.lei2j.sms.bomb.service.impl;

/**
 * @author leijinjun
 * @date 2023/3/15
 **/
public class ScriptThreadContext {

    private static final ThreadLocal<ScriptContext> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(ScriptContext scriptContext){
        THREAD_LOCAL.set(scriptContext);
    }

    public static ScriptContext get(){
        return THREAD_LOCAL.get();
    }

    public static void remove(){
        THREAD_LOCAL.remove();
    }
}
