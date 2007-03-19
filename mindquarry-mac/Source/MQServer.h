//
//  MQServer.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 08.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQJob.h"

@interface MQServer : NSManagedObject {

	NSMutableArray *requestQueue;
	NSLock *requestLock;
	int requestRunningCount;
	
	NSLock *runningLock;
	NSMutableArray *runningRequests;
	
	NSURLCredential *credential;
	NSURLProtectionSpace *protectionSpace;
	
}

- (void)initRequestQueue;

- (void)enqueueRequest:(MQJob *)req;

- (void)runFromQueueIfNeeded:(id)sender;

- (NSMutableArray *)requestQueue;

- (NSLock *)requestLock;

- (void)cancelAll;

- (void)setUsername:(NSString *)username;

- (void)setPassword:(NSString *)password;

- (void)setLocalPath:(NSString *)localPath;

- (void)clearCredentials;

- (NSString *)localPath;

- (NSString *)displayLocalPath;

- (NSURL *)webURL;

@end
