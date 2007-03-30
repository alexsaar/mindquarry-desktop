//
//  SVNController.h
//  Mindquarry SVN
//
//  Created by Jonas on 12.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <JavaVM/jni.h>

#define SVN_STATUS_NORMAL 1
#define SVN_STATUS_MODIFIED 2
#define SVN_STATUS_ADDED 3
#define SVN_STATUS_DELETED 4
#define SVN_STATUS_UNVERSIONED 5
#define SVN_STATUS_MISSING 6
#define SVN_STATUS_CONFLICTED 9

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

- (BOOL)fetchLocalChangesForTeam:(id)team returnError:(NSError **)error;

- (void)_mainThreadChangeInsert:(id)arg;

- (BOOL)fetchRemoteChangesForTeam:(id)team returnError:(NSError **)error;

- (BOOL)commitItems:(NSArray *)items message:(NSString *)message returnError:(NSError **)error;

- (void)setLocalPath:(NSString *)path;

- (BOOL)cancelReturnError:(NSError **)error;

@end
