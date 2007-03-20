//
//  MQSVNCommitJob.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 16.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQSVNCommitJob.h"

#import "MQTeam.h"
#import "SVNController.h"

#import "MQSVNUpdateJob.h"
#import "Mindquarry_Desktop_Client_AppDelegate.h"

@implementation MQSVNCommitJob

- (id)init
{
	if (![super init])
		return nil;
	
	cancel = NO;
	
	return self;
}

- (void)threadMethod
{
	NSEnumerator *teamEnum = [[server valueForKey:@"teams"] objectEnumerator];
	while (!cancel && (currentTeam = [teamEnum nextObject])) {
		[currentTeam initJVM];
		[[currentTeam svnController] attachCurrentThread];

		if (cancel)
			break;
		
//		[currentTeam update];
		
		if (cancel)
			break;
		
		NSArray *changes = [[[currentTeam valueForKey:@"changes"] allObjects] filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"enabled = TRUE"]];
		
		if (cancel)
			break;
		
		NSMutableArray *paths = [NSMutableArray array];
		NSEnumerator *chEnum = [changes objectEnumerator];
		id change;
		while (!cancel && (change = [chEnum nextObject])) {
			[paths addObject:[change valueForKey:@"absPath"]];
		}
				
		if (cancel)
			break;
		
		if ([paths count] > 0)
			[currentTeam commitChanges:paths message:nil];
		
		if (cancel)
			break;
		
		[currentTeam getSVNChanges];
		
	}
	currentTeam = nil;
	
	[[NSApp delegate] setValue:nil forKey:@"cachedMessage"];
	
//	[[[[MQSVNUpdateJob alloc] initWithServer:server updates:NO] autorelease] addToQueue];
}

- (NSString *)statusString
{
	return @"Uploading changes...";
}

- (void)cancel
{
	cancel = YES;
	[[currentTeam svnController] cancelReturnError:nil];
}

@end
