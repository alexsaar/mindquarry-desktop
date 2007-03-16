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
#import "SVNController.h"

@implementation MQSVNUpdateJob

- (void)threadMethod
{

	NSMutableArray *changes = [NSMutableArray array];
	
	NSEnumerator *teamEnum = [[server valueForKey:@"teams"] objectEnumerator];
	id team;
	while (team = [teamEnum nextObject]) {
		
		[team initJVM];
		[[team svnController] attachCurrentThread];
		
		[team update];
		
		[changes addObjectsFromArray:[team changes]];
		
//		[team destroyJVM];
	}

//	NSLog(@"changes: %@", changes);
	
	id controller = [[NSApp delegate] valueForKey:@"changesController"];
	[controller removeObjects:[controller arrangedObjects]];
	[controller addObjects:changes];
	
	[[NSApp delegate] reloadChanges];
	
	[self performSelectorOnMainThread:@selector(finishRequest) withObject:nil waitUntilDone:YES];
}

@end
