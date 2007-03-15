//
//  MQTeam.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 13.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTeam.h"

#import "SVNController.h"

@implementation MQTeam

- (void)initJVM
{
	if (!svn) {
		svn = [[SVNController alloc] initWithRepository:[self valueForKey:@"workspaceURL"] username:[[self valueForKey:@"server"] valueForKey:@"username"] password:[[self valueForKey:@"server"] valueForKey:@"password"] localPath:[self localPath]];
	}
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

- (NSArray *)changes
{
	[self initJVM];
	NSMutableArray *changes;
	[svn getLocalChanges:&changes returnError:nil];
	return changes;
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

@end
