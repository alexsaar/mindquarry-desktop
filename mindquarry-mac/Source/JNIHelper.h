/*
 *  JNIHelper.h
 *  Mindquarry SVN
 *
 *  Created by Jonas on 09.03.07.
 *  Copyright 2007 __MyCompanyName__. All rights reserved.
 *
 */

#import <JavaVM/jni.h>
#import <Cocoa/Cocoa.h>

// The CHECK_EXCEPTION macro checks if a Java Exception occurred
// and prints out the exception and clears it, if necessary.
//
// When called via JNI, Java Code will not automatically print exceptions
// and continue operation normally. However, if an uncleared exceptions
// exists somewhere, all subsequent JNI calls will fail miserably.
// 
// It remains to be seen if the macro can safely be disabled for Release
// builds.

// #ifdef DEBUG
#define CHECK_EXCEPTION if (env->ExceptionCheck() == JNI_TRUE) { \
    env->ExceptionDescribe(); \
		env->ExceptionClear(); \
}
//#else
//#define CHECK_EXCEPTION
//#endif

// The GET_FIELD macro automates the lookup of a jfieldID and caches the
// result to improve performance.
// 
// A static jfieldID variable named <var_name> will be declared that you 
// can use after calling this macro.

#define GET_FIELD(var_name, clazz, field_name, field_sig, ret_val) static jfieldID var_name; \
if (!var_name) { \
    var_name = env->GetFieldID(clazz, field_name, field_sig); \
		if (!var_name) { \
			NSLog(@"Could not get %s field", field_name); \
				CHECK_EXCEPTION \
				return ret_val; \
		} \
}

// Functions to convert Java strings to NSStrings and backwards.
NSString* jstring_to_nsstring(JNIEnv *env, jstring string);
jstring nsstring_to_jstring(JNIEnv *env, NSString *string);
