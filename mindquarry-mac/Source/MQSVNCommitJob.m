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

- (void)threadMethod
{
	NSEnumerator *teamEnum = [[server valueForKey:@"teams"] objectEnumerator];
	id team;
	while (team = [teamEnum nextObject]) {
		[team initJVM];
		[[team svnController] attachCurrentThread];

		NSArray *changes = [[[team valueForKey:@"changes"] allObjects] filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"enabled = TRUE"]];
		
		NSMutableArray *paths = [NSMutableArray array];
		NSEnumerator *chEnum = [changes objectEnumerator];
		id change;
		while (change = [chEnum nextObject]) {
			[paths addObject:[change valueForKey:@"absPath"]];
		}
				
		if ([paths count] > 0)
			[team commitChanges:paths message:nil];
		
//		[team update];
//		[team getSVNChanges];
	}

	[[NSApp delegate] setValue:nil forKey:@"cachedMessage"];
	
	[[[[MQSVNUpdateJob alloc] initWithServer:server] autorelease] addToQueue];
	
	[self performSelectorOnMainThread:@selector(finishRequest) withObject:nil waitUntilDone:NO];
}

- (NSString *)statusString
{
	return @"Commiting changes...";
}

@end
