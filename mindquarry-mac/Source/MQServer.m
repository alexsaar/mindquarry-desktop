//
//  MQServer.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 08.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQServer.h"

#import "MQRequest.h"
#import "MQTeam.h"

@implementation MQServer

- (id)init
{
	if (![super init])
		return nil;
	
	[self initRequestQueue];
	
	return self;
}

- (void)awakeFromFetch
{
	[self initRequestQueue];
}

- (void)awakeFromInsert
{
	[self initRequestQueue];
}

- (void)initRequestQueue
{
	if (!requestQueue)
		requestQueue = [[NSMutableArray alloc] init];
	if (!requestLock)
		requestLock = [[NSLock alloc] init];
	if (!runningLock)
		runningLock = [[NSLock alloc] init];
	if (!runningRequests)
		runningRequests = [[NSMutableArray alloc] init];
}

- (void)enqueueRequest:(MQRequest *)req
{
	[requestLock lock];
	
	if (requestRunningCount < MAX_CONNECTION) {
		requestRunningCount++;
		[MQRequest increaseRequestCount:req];
		
		[runningLock lock];
		[req startRequest];		
		[runningRequests addObject:req];
		[runningLock unlock];
	}
	else {
		//		NSLog(@"enqueuing request");
		[requestQueue addObject:req];		
	}
	
	[requestLock unlock];
}

- (void)runFromQueueIfNeeded:(id)sender;
{
	if (sender) {
		[runningLock lock];
		[runningRequests removeObject:sender];
		[runningLock unlock];	
	}
	
	id request = nil;
	
	[requestLock lock];
	requestRunningCount--;
	if ([requestQueue count] > 0) {
		request = [[requestQueue objectAtIndex:0] retain]; 
		[requestQueue removeObjectAtIndex:0];
	}
	[requestLock unlock];
	
	if (request) {
		[requestLock lock];
		requestRunningCount++;
		[MQRequest increaseRequestCount:request];
		[requestLock unlock];
		
		[runningLock lock];
		[request startRequest];
		[runningRequests addObject:request];
		[request release];		
		[runningLock unlock];
	}
}

- (NSMutableArray *)requestQueue
{
	return requestQueue;
}

- (NSLock *)requestLock
{
	return requestLock;
}

- (void)cancelAll
{
	[requestLock lock];
	[runningLock lock];
	
	[requestQueue removeAllObjects];
	
	NSEnumerator *rreqs = [runningRequests objectEnumerator];
	id req;
	while (req = [rreqs nextObject]) {
		[req cancel];
		[MQRequest decreaseRequestCount];
	}
	
	[runningRequests removeAllObjects];
	
	[runningLock unlock];
	[requestLock unlock];
}

- (void)setUsername:(NSString *)username
{
	[self willChangeValueForKey:@"username"];
	[self setPrimitiveValue:username forKey:@"username"];
	[self clearCredentials];
	[self didChangeValueForKey:@"username"];
}

- (void)setPassword:(NSString *)password
{
	[self willChangeValueForKey:@"password"];
	[self setPrimitiveValue:password forKey:@"password"];
	[self clearCredentials];
	[self didChangeValueForKey:@"password"];
}

- (void)setLocalPath:(NSString *)localPath
{
	[self willChangeValueForKey:@"localPath"];
	[self setPrimitiveValue:localPath forKey:@"localPath"];
	NSEnumerator *teamEnum = [[self valueForKey:@"teams"] objectEnumerator];
	id team;
	while (team = [teamEnum nextObject]) {
		[team updateLocalPath];
	}
	[self didChangeValueForKey:@"localPath"];
}

- (void)clearCredentials
{
	if (credential && protectionSpace) {
		NSURLCredentialStorage *store = [NSURLCredentialStorage sharedCredentialStorage];
		[store removeCredential:credential forProtectionSpace:protectionSpace];
		[self setValue:nil forKey:@"credential"];
		[self setValue:nil forKey:@"protectionSpace"];
	}
}

- (void)updateAllRepositories
{
	NSEnumerator *teamEnum = [[self valueForKey:@"teams"] objectEnumerator];
	id team;
	while (team = [teamEnum nextObject]) {
		[team update];
	}
}

@end
