//
//  MQServer.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 08.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQRequest.h"

@interface MQServer : NSManagedObject {

	NSMutableArray *requestQueue;
	NSLock *requestLock;
	int requestRunningCount;
	
}

- (void)initRequestQueue;

- (void)enqueueRequest:(MQRequest *)req;

- (void)runFromQueueIfNeeded;

- (NSMutableArray *)requestQueue;

- (NSLock *)requestLock;

@end
