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

- (id)initWithServer:(id)_server updates:(BOOL)_update
{
	if (![super initWithServer:_server])
		return nil;
	
	update = _update;
	
	return self;
}

- (void)threadMethod
{
	NSEnumerator *teamEnum = [[server valueForKey:@"teams"] objectEnumerator];
	id team;
	while (team = [teamEnum nextObject]) {
		[team initJVM];
		[[team svnController] attachCurrentThread];
		
		if (update)
			[team update];
		[team getSVNChanges];
	}
	
	[self performSelectorOnMainThread:@selector(finishRequest) withObject:nil waitUntilDone:NO];
}

- (NSString *)statusString
{
	return @"Updating working copy...";
}

@end
