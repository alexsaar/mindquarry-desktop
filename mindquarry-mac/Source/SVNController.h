//
//  SVNController.h
//  Mindquarry SVN
//
//  Created by Jonas on 12.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <JavaVM/jni.h>

@interface SVNController : NSObject {

	NSString *repository;
	NSString *username;
	NSString *password;
	NSString *localPath;
	
	@protected
	jclass helperClass;
	jobject helperRef;
	
	JNIEnv *env;
	
}

- (id)initWithRepository:(NSString *)_repo username:(NSString *)_user password:(NSString *)_pass localPath:(NSString *)_local;

- (void)attachCurrentThread;

- (void)setJavaEnv:(JNIEnv *)_env;

- (BOOL)updateReturnError:(NSError **)error;

- (BOOL)getLocalChanges:(NSMutableArray **)changes returnError:(NSError **)error;

- (BOOL)commitItems:(NSArray *)items message:(NSString *)message returnError:(NSError **)error;

- (void)setLocalPath:(NSString *)path;

@end
