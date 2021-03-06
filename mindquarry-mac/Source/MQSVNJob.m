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

#import "GrowlNotifications.h"

static MQSVNJob *currentJob = nil;

@implementation MQSVNJob

+ (void)cancelCurrentJob
{
    [currentJob setValue:[NSNumber numberWithBool:YES] forKey:@"cancel"];
}

- (id)init
{
	if (![super init])
		return nil;
	
	cancel = NO;
	
	return self;
}

- (id)initWithServer:(id)_server selectedTeam:(id)_selectedTeam predicate:(NSPredicate *)_pred synchronizes:(BOOL)_synchronizes;
{
	if (![super initWithServer:_server])
		return nil;
	
	selectedTeam = [_selectedTeam retain];
	
	synchronizes = _synchronizes;
    predicate = [_pred retain];
	
	return self;
}

- (void)dealloc
{
    [predicate release];
    predicate = nil;
	
	[selectedTeam autorelease];
	selectedTeam = nil;
	
    [super dealloc];
}

- (void)threadMethod
{
	BOOL opened = NO;
    
    currentJob = self;
	
	int count = 0;
	
	NSEnumerator *teamEnum = [[server valueForKey:@"teams"] objectEnumerator];
	while (!cancel && (currentTeam = [teamEnum nextObject])) {
		
		if (selectedTeam && currentTeam != selectedTeam)
			continue;
		
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
            if (predicate)
                allItems = [allItems filteredArrayUsingPredicate:predicate];
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
				count++;
			}
//			NSLog(@"down   %@", updatePaths);
			BOOL ok = NO;
			if (([allItems count] == 0 || [allUpdateItems count] == [updatePaths count]) && !cancel) {
				ok = [[currentTeam svnController] updateReturnError:nil];
			}
			else if ([updatePaths count] > 0 && !cancel) {
				ok = [[currentTeam svnController] updateSelectedItems:updatePaths];
			}
			if (ok && !cancel) {
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
	
    currentJob = nil;
    
	// send a growl notification
	if (synchronizes) {
		[GrowlApplicationBridge notifyWithTitle:@"File Synchronization Completed"
									description:[NSString stringWithFormat:@"%d files", count]
							   notificationName:GROWL_FILES_SYNCHRONIZED
									   iconData:nil
									   priority:0
									   isSticky:NO
								   clickContext:GROWL_FILES_SYNCHRONIZED];
	}
	else {
		[GrowlApplicationBridge notifyWithTitle:@"File Status Updated"
									description:[NSString stringWithFormat:@"%d files", count]
							   notificationName:GROWL_FILE_STATUS_UPDATED
									   iconData:nil
									   priority:0
									   isSticky:NO
								   clickContext:GROWL_FILE_STATUS_UPDATED];
	}
	
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
