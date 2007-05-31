/*
 *  JNIHelper.cpp
 *  Mindquarry SVN
 *
 *  Created by Jonas on 09.03.07.
 *  Copyright 2007 __MyCompanyName__. All rights reserved.
 *
 */

#include "JNIHelper.h"

jlong id_to_jlong(id object) {
    return (jlong) object;
}

id jlong_to_id(jlong object) {
    return (id)(void*) object;
}

NSString* jstring_to_nsstring(JNIEnv *env, jstring string) {
    
    if (!string)
        return nil;
    
    jboolean iscopy = JNI_TRUE;
    const char *chars = env->GetStringUTFChars(string, &iscopy);
    
    if (!chars)
        return nil;
    
    if (!iscopy)
        NSLog(@"Warning: jstring_to_nsstring iscopy = JNI_FALSE (%s:%s)", __FILE__, __LINE__);
    
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        return nil;
    }
    
    NSString *res = [NSString stringWithCString:chars encoding:NSUTF8StringEncoding];
    
    env->ReleaseStringUTFChars(string, chars);
    
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        return nil;
    }
    
    return res;
}

jstring nsstring_to_jstring(JNIEnv *env, NSString *string) {
    
    if (!string)
        return nil;
    
    jstring result = env->NewStringUTF([string UTF8String]);
    
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        return nil;
    }
    
    return result;
}


