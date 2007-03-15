//
//  MQSVNUpdateJob.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 15.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQSVNUpdateJob.h"

#import "MQTeam.h"
#import "MQServer.h"
#import "Mindquarry_Desktop_Client_AppDelegate.h"

@implementation MQSVNUpdateJob

- (void)threadMethod
{
	[server updateAllRepositories];
	
	NSMutableArray *changes = [NSMutableArray array];
	
	NSEnumerator *teamEnum = [[server valueForKey:@"teams"] objectEnumerator];
	id team;
	while (team = [teamEnum nextObject]) {
		[changes addObjectsFromArray:[team changes]];
	}

//	NSLog(@"changes: %@", changes);
	
	id controller = [[NSApp delegate] valueForKey:@"changesController"];
	[controller removeObjects:[controller arrangedObjects]];
	[controller addObjects:changes];
	
	[[NSApp delegate] reloadChanges];
	
	[self performSelectorOnMainThread:@selector(finishRequest) withObject:nil waitUntilDone:YES];
}

@end
