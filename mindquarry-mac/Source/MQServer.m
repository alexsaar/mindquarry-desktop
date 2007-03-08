//
//  MQServer.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 08.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQServer.h"

#import "MQRequest.h"

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

- (void)runFromQueueIfNeeded
{
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
		[request autorelease];		
		[runningRequests addObject:request];
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

@end
