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
	cancel = NO;
	
	return self;
}

- (void)threadMethod
{
	NSEnumerator *teamEnum = [[server valueForKey:@"teams"] objectEnumerator];
	while (!cancel && (currentTeam = [teamEnum nextObject])) {
		[currentTeam initJVM];
		[[currentTeam svnController] attachCurrentThread];

		if (!cancel && update)
			[currentTeam update];
		if (!cancel)
			[currentTeam getSVNChanges];		
	}
	currentTeam = nil;
}

- (NSString *)statusString
{
	return @"Downloading files...";
}

- (void)cancel
{
	cancel = YES;
	[[currentTeam svnController] cancelReturnError:nil];
}

@end
