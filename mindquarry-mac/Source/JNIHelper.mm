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
    const char *chars = env->GetStringUTFChars(string, NULL);
    NSString *res = [NSString stringWithCString:chars encoding:NSUTF8StringEncoding];
    env->ReleaseStringUTFChars(string, chars);
    return res;
}

jstring nsstring_to_jstring(JNIEnv *env, NSString *string) {
    if (!string)
        return nil;
    return env->NewStringUTF([string UTF8String]);
}


