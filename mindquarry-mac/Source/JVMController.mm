//
//  JVMController.m
//  Mindquarry SVN
//
//  Created by Jonas on 09.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "JVMController.h"

#import "JNIHelper.h"
#import "MacSVNHelperNative.h"

static JavaVM *_jvm_controller_jvm = nil;

JNIEnv *env;

@implementation JVMController

+ (void)initialize
{
	env = nil;
}

+ (JNIEnv *)JNIEnv
{
	return env;
}

+ (JavaVM *)JavaVM
{
	return _jvm_controller_jvm;
}

+ (BOOL)createJVMIfNeeded;
{
	if (_jvm_controller_jvm || env)
        return TRUE;
	
	struct JavaVMInitArgs vm_args;
    vm_args.version = JNI_VERSION_1_4;
    vm_args.ignoreUnrecognized = TRUE;
    
	NSString *classPath = [[NSBundle mainBundle] pathForResource:@"mindquarry-mac-svn" ofType:@"jar"];
	if (!classPath) {
		NSLog(@"Warning: SVN jar file not found!");
	}
	
	NSString *classpathArgString;
    if (classPath) {
        // We need to set the bootclasspath, not the regular classpath
        classpathArgString = [NSString stringWithFormat:@"-Xbootclasspath/a:%@", classPath];
		
        struct JavaVMOption jvmOptions[1];
        jvmOptions[0].optionString = (char*) [classpathArgString UTF8String];
        jvmOptions[0].extraInfo = NULL;
        		        
        vm_args.options = jvmOptions;
        vm_args.nOptions = 1;
    }
    else {
        vm_args.options = NULL;
        vm_args.nOptions = 0;
    }

	if (JNI_CreateJavaVM(&_jvm_controller_jvm, (void**)&env, &vm_args) != 0) {
        NSLog(@"Error: Failed to create JVM");
        return FALSE;
    }
    
    CHECK_EXCEPTION;
	
	if (![self registerNatives])
		return NO;

	CHECK_EXCEPTION;
	
	return TRUE;
}

+ (BOOL)registerNatives
{
	// JNIHandler methods
    jclass jJNIHandler = env->FindClass("com/mindquarry/desktop/workspace/MacSVNHelper");
    if (!jJNIHandler) {
        NSLog(@"Failed to find MacSVNClient class");
        return FALSE;
    }    
    JNINativeMethod jniHandlerMethods[] = {
		{ (char*)"getCommitMessage", (char*)"()Ljava/lang/String;", (void*)&Java_com_mindquarry_desktop_svn_MacSVNHelper_getCommitMessage },
    };
    if (env->RegisterNatives(jJNIHandler, jniHandlerMethods, sizeof(jniHandlerMethods) / sizeof(JNINativeMethod)) != 0) {
        NSLog(@"could not register MacSVNClient native methods");
        return FALSE;
    }
	CHECK_EXCEPTION;
	
	return YES;
}

+ (BOOL)destroyJVM
{
	if (!_jvm_controller_jvm) {
        NSLog(@"Warning: No JVM present");
        return FALSE;
    }
    if (_jvm_controller_jvm->DestroyJavaVM() == 0) {
        _jvm_controller_jvm = nil;
        env = nil;
        return TRUE;
    }
    return FALSE;
}

@end
