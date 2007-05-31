//
//  MQThreadJob.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 15.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQThreadJob.h"


@implementation MQThreadJob

- (void)startRequest
{
	[NSThread detachNewThreadSelector:@selector(_threadHelper) toTarget:self withObject:nil];
}

- (void)_threadHelper
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

	didFree = NO;
	[self retain];
	
	[self threadMethod];

	[self performSelectorOnMainThread:@selector(finishRequest) withObject:nil waitUntilDone:YES];
	
	[pool release];
}

- (void)threadMethod
{
	
}

@end
