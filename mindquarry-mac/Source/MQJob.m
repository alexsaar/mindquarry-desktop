//
//  MQJob.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 15.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQJob.h"

#import "MQServer.h"

static NSLock *spinner_lock = nil;
static int request_running_count = 0;

@implementation MQJob

+ (void)initialize
{
	spinner_lock = [[NSLock alloc] init];
}

+ (void)increaseRequestCount:(id)sender
{
	[spinner_lock lock];
	
	BOOL wasZero = request_running_count == 0;
	//	NSLog(@"++ request_running_count %d" ,request_running_count);
	
	request_running_count++;
	
	if (wasZero) {
		
		//		NSLog(@" === start busy");
		
		id spinner = [[NSApp delegate] valueForKey:@"statusSpinner"];
		[spinner setHidden:NO];
		[spinner startAnimation:self];
		
		NSString *message = nil;
		
		if ([sender respondsToSelector:@selector(statusString)])
			message = [sender statusString];
		else {
			//		NSLog(@"sender %@ has no msg", sender);
			message = @"request...";		
		}
		
		id field = [[NSApp delegate] valueForKey:@"statusField"];
		[field setStringValue:message];
		[field setHidden:NO];		
		
		id tbItem = [[NSApp delegate] valueForKey:@"refreshToolbarItem"];		
		[tbItem setEnabled:NO];
		//		[tbItem setImage:[NSImage imageNamed:@"AlertStopIcon"]];
		//		[tbItem setLabel:@"Stop"];
		
		[[[NSApp delegate] valueForKey:@"commitFilesToolbarItem"] setEnabled:NO];
		[[[NSApp delegate] valueForKey:@"commitTasksToolbarItem"] setEnabled:NO];
		
		id stopItem = [[NSApp delegate] valueForKey:@"stopToolbarItem"];
		[stopItem setEnabled:YES];
	}
	
	[spinner_lock unlock];
}

+ (void)decreaseRequestCount:(id)sender
{
	[spinner_lock lock];
	
	//	NSLog(@"-- request_running_count %d" ,request_running_count);
	
	request_running_count--;
	
	if (request_running_count == 0) {
		
		//		NSLog(@" === stop busy");
		
		id spinner = [[NSApp delegate] valueForKey:@"statusSpinner"];
		[spinner stopAnimation:self];
		[spinner setHidden:YES];
		
		id field = [[NSApp delegate] valueForKey:@"statusField"];
		[field setHidden:YES];
		
		id tbItem = [[NSApp delegate] valueForKey:@"refreshToolbarItem"];		
		[tbItem setEnabled:YES];
		
		[[[NSApp delegate] valueForKey:@"commitFilesToolbarItem"] setEnabled:YES];
		[[[NSApp delegate] valueForKey:@"commitTasksToolbarItem"] setEnabled:YES];
		
		id stopItem = [[NSApp delegate] valueForKey:@"stopToolbarItem"];
		[stopItem setEnabled:NO];
	}
	else if (sender) {
		id field = [[NSApp delegate] valueForKey:@"statusField"];
		[field setStringValue:[sender statusString]];
	}
	
	[spinner_lock unlock];
}

- (id)initWithServer:(id)_server
{
	if (![super init])
		return nil;
	
	server = [_server retain];
	
	didFree = YES;
	
	return self;
}

- (void)dealloc
{
	[server release];
	
	[super dealloc];
}

- (void)addToQueue
{
	[server enqueueRequest:self];
}

- (void)startRequest
{
	
}

- (void)finishRequest
{
	if (!didFree) {
		[server runFromQueueIfNeeded:self];
		[MQJob decreaseRequestCount:self];

		[self autorelease];
		
		didFree = YES;
	}

}

- (void)cancel
{
	NSLog(@"Warning: cancel not implemented");
}

- (NSString *)statusString
{
	return @"<status>";
}

@end
