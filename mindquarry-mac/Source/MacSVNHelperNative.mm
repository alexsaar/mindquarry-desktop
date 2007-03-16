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

JNIEXPORT jstring JNICALL Java_com_mindquarry_desktop_svn_MacSVNHelper_getCommitMessage
(JNIEnv *env, jobject jhelper)
{
	NSString *message = [[NSApp delegate] getCommitMessage];

	return nsstring_to_jstring(env, message);
}
