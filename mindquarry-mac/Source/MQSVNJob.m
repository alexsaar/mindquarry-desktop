//
//  MQSVNJob.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 02.04.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQSVNJob.h"

#import "MQTeam.h"
#import "SVNController.h"

#import "Mindquarry_Desktop_Client_AppDelegate.h"

@implementation MQSVNJob

- (id)init
{
	if (![super init])
		return nil;
	
	cancel = NO;
	
	return self;
}

- (id)initWithServer:(id)_server synchronizes:(BOOL)_synchronizes
{
	if (![super initWithServer:_server])
		return nil;
	
	synchronizes = _synchronizes;
	
	return self;
}

- (void)threadMethod
{
	BOOL opened = NO;
	
	NSEnumerator *teamEnum = [[server valueForKey:@"teams"] objectEnumerator];
	while (!cancel && (currentTeam = [teamEnum nextObject])) {
		[currentTeam initJVM];
		[[currentTeam svnController] attachCurrentThread];
		
//		NSLog(@"svn job %@ %@", synchronizes ? @"sync" : @"", [currentTeam valueForKey:@"name"]);
		
		NSMutableArray *deleteObjects = [NSMutableArray array];
		if (synchronizes) {		
			
			if (!opened) {
				[[NSApp delegate] performSelectorOnMainThread:@selector(setProgressVisible:) withObject:[NSNumber numberWithBool:YES] waitUntilDone:NO];
				opened = YES;
			}
			
			if (cancel)
				break;
			// get commit items, add them
			NSArray *allItems = [[currentTeam valueForKey:@"changes"] allObjects];
//			NSLog(@"all  %@", allItems);
			NSArray *commitItems = [allItems filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"enabled = TRUE AND local = TRUE"]];
			NSMutableArray *commitPaths = [NSMutableArray array];
			NSEnumerator *chEnum = [commitItems objectEnumerator];
			id change;
			while (!cancel && (change = [chEnum nextObject])) {
				[commitPaths addObject:[change valueForKey:@"absPath"]];
			}
//			NSLog(@"up %@", commitPaths);
			if ([commitPaths count] > 0) {
				[[currentTeam svnController] addSelectedItems:commitPaths];
			}
			
			if (cancel)
				break;
			// get update items, update them
			NSArray *allUpdateItems = [allItems filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"onServer = TRUE"]];
			NSArray *updateItems = [allItems filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"enabled = TRUE AND onServer = TRUE"]];
			NSMutableArray *updatePaths = [NSMutableArray array];
			chEnum = [updateItems objectEnumerator];
			while (!cancel && (change = [chEnum nextObject])) {
				[updatePaths addObject:[change valueForKey:@"absPath"]];
			}
//			NSLog(@"down   %@", updatePaths);
			BOOL ok = NO;
			if ([allItems count] == 0 || [allUpdateItems count] == [updatePaths count]) {
				ok = [[currentTeam svnController] updateReturnError:nil];
			}
			else if ([updatePaths count] > 0) {
				ok = [[currentTeam svnController] updateSelectedItems:updatePaths];
			}
			if (ok) {
				[deleteObjects addObjectsFromArray:updateItems];
			}
			
			if (cancel)
				break;			
			// commit selected items
			if ([commitPaths count] > 0) {
				if ([[currentTeam svnController] commitItems:commitPaths message:nil returnError:nil]) {
					[deleteObjects addObjectsFromArray:commitItems];
				}
			}
		}
		else {
			NSEnumerator *chEnum = [[currentTeam valueForKey:@"changes"] objectEnumerator];
			id change;
			while (!cancel && (change = [chEnum nextObject]))
				[change setValue:[NSNumber numberWithBool:YES] forKey:@"stale"];

			if (cancel)
				break;
			// get remote changes
			[[currentTeam svnController] fetchRemoteChangesForTeam:currentTeam returnError:nil];
			
			if (cancel)
				break;
			// get local changes
			[[currentTeam svnController] fetchLocalChangesForTeam:currentTeam returnError:nil];
			
			[deleteObjects addObjectsFromArray:[[[currentTeam valueForKey:@"changes"] allObjects] filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"stale = YES"]]];
		}
		
//		NSLog(@"delete %@", deleteObjects);
		if (!cancel && [deleteObjects count]) {
			// remove old changes
			id chController = [[NSApp delegate] valueForKey:@"changesController"];
			NSEnumerator *chEnum = [deleteObjects objectEnumerator];
			id change;
			while (!cancel && (change = [chEnum nextObject])) 
				[chController performSelectorOnMainThread:@selector(removeObject:) withObject:change waitUntilDone:YES];
		}
	}
	
	currentTeam = nil;
	
	[[NSApp delegate] setValue:nil forKey:@"cachedMessage"];
	
	//	[[[[MQSVNUpdateJob alloc] initWithServer:server updates:NO] autorelease] addToQueue];
	
	[[NSApp delegate] performSelectorOnMainThread:@selector(setProgressVisible:) withObject:[NSNumber numberWithBool:NO] waitUntilDone:NO];
}

- (NSString *)statusString
{
	return @"Uploading changes...";
}

- (void)cancel
{
	cancel = YES;
	[[currentTeam svnController] cancelReturnError:nil];
	
	[[NSApp delegate] performSelectorOnMainThread:@selector(setProgressVisible:) withObject:[NSNumber numberWithBool:NO] waitUntilDone:NO];
}

@end
