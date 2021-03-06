//
//  MQTeam.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 13.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTeam.h"

#import "SVNController.h"
#import "MQServer.h"

@implementation MQTeam

- (void)initJVM
{
	if (!svn) {
		NSLog(@"(initWithRepository): %@ -> %@", [self valueForKey:@"workspaceURL"], [self localPath]);
		svn = [[SVNController alloc] initWithRepository:[self valueForKey:@"workspaceURL"] repository:[self valueForKey:@"workspaceURL"] username:[[self valueForKey:@"server"] valueForKey:@"username"] password:[[self valueForKey:@"server"] valueForKey:@"password"] localPath:[self localPath]];
	}
}

- (void)destroyJVM
{
	[svn release];
	svn = nil;
}

- (SVNController *)svnController
{
	return svn;
}

- (NSString *)localPath
{
	NSString *base = [[self valueForKey:@"server"] valueForKey:@"localPath"];
	NSMutableString *name = [[[self valueForKey:@"name"] mutableCopy] autorelease];
	[name replaceOccurrencesOfString:@"/" withString:@" " options:0 range:NSMakeRange(0, [name length])];
	return [base stringByAppendingPathComponent:name];
}

- (void)update
{
	[self initJVM];
	[svn updateReturnError:nil];
}

- (void)getSVNChanges
{
	id chController = [[NSApp delegate] valueForKey:@"changesController"];
//	NSManagedObjectContext *context = [[NSApp delegate] managedObjectContext];
	NSEnumerator *chEnum = [[self valueForKey:@"changes"] objectEnumerator];
	id ch;
	while (ch = [chEnum nextObject]) {
//		[context performSelectorOnMainThread:@selector(deleteObject:) withObject:ch waitUntilDone:YES];
		[chController performSelectorOnMainThread:@selector(removeObject:) withObject:ch waitUntilDone:YES];
	}
	
	[self initJVM];

	[svn fetchRemoteChangesForTeam:self returnError:nil];

	[svn fetchLocalChangesForTeam:self returnError:nil];


}

- (void)commitChanges:(NSArray *)changes message:(NSString *)commitMessage
{
	[self initJVM];
	[svn commitItems:changes message:commitMessage returnError:nil];
}

- (void)updateLocalPath
{
	[svn setLocalPath:[self localPath]];
}

- (NSURL *)tasksURL
{
	return [NSURL URLWithString:[NSString stringWithFormat:@"tasks/%@/", [self valueForKey:@"id"]] relativeToURL: [[self valueForKey:@"server"] webURL]];
}

@end
