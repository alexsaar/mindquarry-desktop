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
}

- (void)enqueueRequest:(MQRequest *)req
{
	[requestLock lock];
	
	if (requestRunningCount < MAX_CONNECTION) {
		requestRunningCount++;
		[MQRequest increaseRequestCount:self];
		[req startRequest];
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
		[MQRequest increaseRequestCount:request];
		[request startRequest];
		[request autorelease];
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

@end
