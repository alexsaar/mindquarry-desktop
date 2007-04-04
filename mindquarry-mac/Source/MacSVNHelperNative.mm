//
//  MacSVNHelperNative.mm
//  Mindquarry Desktop Client
//
//  Created by Jonas on 16.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MacSVNHelperNative.h"
#import "JNIHelper.h"
#import "Mindquarry_Desktop_Client_AppDelegate.h"

JNIEXPORT void JNICALL Java_com_mindquarry_desktop_workspace_MacSVNHelper_onNotify
(JNIEnv *env, jobject jhelper, jobject jinfo)
{

	static jclass notifyInfoClass = nil;
	if (!notifyInfoClass) {
		notifyInfoClass = env->FindClass("org/tigris/subversion/javahl/NotifyInformation");
		CHECK_EXCEPTION;
		if (!notifyInfoClass) {
			NSLog(@"Could not find NotifyInfo class");
			return;
		}		
	}

	static jmethodID getPathMethod = nil;
	if (!getPathMethod) {
		getPathMethod = env->GetMethodID(notifyInfoClass, "getPath", "()Ljava/lang/String;");
		CHECK_EXCEPTION;
		if (!getPathMethod) {
			NSLog(@"Could not get getPath method");
			return;
		}
	}
	
	static jmethodID getActionMethod = nil;
	if (!getActionMethod) {
		getActionMethod = env->GetMethodID(notifyInfoClass, "getAction", "()I");
		CHECK_EXCEPTION;
		if (!getActionMethod) {
			NSLog(@"Could not get getAction method");
			return;
		}
	}
	
	jstring jfile = (jstring) env->CallObjectMethod(jinfo, getPathMethod);
	CHECK_EXCEPTION;

	jint action = env->CallIntMethod(jinfo, getActionMethod);
	CHECK_EXCEPTION;
	
	static NSString *lastFile = nil;
	NSString *file = jstring_to_nsstring(env, jfile);	
	
	if (action > 7 && (!lastFile || ![lastFile isEqualToString:file])) {
		
		NSString *actionInfo = nil;
		switch (action) {
			case 0: case 9: case 16:
				actionInfo = @"Add";
				break;
			case 2: case 8: case 17:
				actionInfo = @"Delete";
				break;
			case 10:
				actionInfo = @"Update";
				break;
			case 15:
				actionInfo = @"Modify";
				break;
			case 6: case 18: case 3: case 4: case 5:
				actionInfo = @"Revert";
				break;
			case 1:
				actionInfo = @"Copy";
				break;
			default:
				break;
		}
		
		if (actionInfo) {
			NSLog(@"mac notify: %@ - %@", file, actionInfo);
			[[NSApp delegate] addProgressPath:file withAction:actionInfo];			
		}
	}
	
	id tmp = lastFile;
	lastFile = [file copy];
	[tmp release];
}

JNIEXPORT jstring JNICALL Java_com_mindquarry_desktop_workspace_MacSVNHelper_getCommitMessage
(JNIEnv *env, jobject jhelper)
{
	NSString *message = [[NSApp delegate] getCommitMessage];
	if (!message)
		return nil;
	return nsstring_to_jstring(env, message);
}
