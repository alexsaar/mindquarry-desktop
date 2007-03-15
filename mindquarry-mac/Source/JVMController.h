//
//  JVMController.h
//  Mindquarry SVN
//
//  Created by Jonas on 09.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import <JavaVM/jni.h>

@interface JVMController : NSObject {

	@protected
	NSString *classpath;
	JavaVM *jvm;
	JNIEnv *env;
	
}

+ (JNIEnv *)JNIEnv;
+ (JavaVM *)JavaVM;

+ (BOOL)createJVMIfNeeded;
+ (BOOL)destroyJVM;

@end
