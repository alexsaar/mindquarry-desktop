//
//  SVNController.h
//  Mindquarry SVN
//
//  Created by Jonas on 12.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <JavaVM/jni.h>

#define SVN_STATUS_NONE 0
#define SVN_STATUS_NORMAL 1
#define SVN_STATUS_MODIFIED 2
#define SVN_STATUS_ADDED 3
#define SVN_STATUS_DELETED 4
#define SVN_STATUS_UNVERSIONED 5
#define SVN_STATUS_MISSING 6
#define SVN_STATUS_REPLACED 7
#define SVN_STATUS_MERGED 8
#define SVN_STATUS_CONFLICTED 9
#define SVN_STATUS_OBSTRUCTED 10
#define SVN_STATUS_IGNORED 11
#define SVN_STATUS_INCOMPLETE 12
#define SVN_STATUS_EXTERNAL 13

#define SVN_STATUS_DOWNLOAD 32

@interface SVNController : NSObject {

	NSString *repository;
	NSString *username;
	NSString *password;
	NSString *localPath;
	
	@protected
	jclass helperClass;
	jobject helperRef;
	
	NSString *relativePath;
	
	JNIEnv *env;
	
}

- (id)initWithRepository:(NSString *)_repo repository:(NSString *)_repository username:(NSString *)_user password:(NSString *)_pass localPath:(NSString *)_local;

- (NSString *)relativePath;

- (void)attachCurrentThread;

- (void)setJavaEnv:(JNIEnv *)_env;

- (BOOL)updateReturnError:(NSError **)error;

- (BOOL)updateSelectedItems:(NSArray *)items;

- (BOOL)addSelectedItems:(NSArray *)items;

- (BOOL)fetchLocalChangesForTeam:(id)team returnError:(NSError **)error;

- (void)_mainThreadChangeInsert:(id)arg;

- (BOOL)fetchRemoteChangesForTeam:(id)team returnError:(NSError **)error;

- (BOOL)commitItems:(NSArray *)items message:(NSString *)message returnError:(NSError **)error;

- (void)setLocalPath:(NSString *)path;

- (BOOL)cancelReturnError:(NSError **)error;

@end
