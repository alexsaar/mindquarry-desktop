//
//  MQTasksRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTasksRequest.h"

#import "RequestController.h"
#import "MQTaskPropertiesRequest.h"

#import "GrowlNotifications.h"

@implementation MQTasksRequest

- (id)initWithServer:(id)_server forTeam:(id)_team;
{
	if (![super initWithServer:_server])
		return nil;
	
	team = [_team retain];

	return self;
}

- (void)dealloc
{
	[team release];
	team = nil;
	
	[super dealloc];
}

- (NSURL *)url
{
	NSString  *teamID = [team valueForKey:@"id"];
	if (!teamID)
		return nil;
	return [self currentURLForPath:[NSString stringWithFormat:@"tasks/%@/", teamID]];
}

- (void)parseXMLResponse:(NSXMLDocument *)document
{
	// mark all tasks stale	
	NSEnumerator *taskEnum = [[[team valueForKey:@"tasks"] allObjects] objectEnumerator];
	id task;
	while (task = [taskEnum nextObject])
		[task setValue:[NSNumber numberWithBool:NO] forKey:@"upToDate"];
		
	NSXMLElement *root = [document rootElement];
	
	int task_count = 0;
	
	int i;
	int count = [root childCount];
	for (i = 0; i < count; i++) {
		id node = [root childAtIndex:i];
		if (![[node name] isEqualToString:@"task"])
			continue;
		if (![node isKindOfClass:[NSXMLElement class]])
			continue;
		
		NSString *obj_id = [[node attributeForName:@"xlink:href"] stringValue];
		
		if (!obj_id)
			continue;
		
//		NSLog(@"task %@ name %@", node, obj_id);
		
		id taskobj = [[[NSApp delegate] valueForKey:@"controller"] taskWithId:obj_id forTeam:team];
		[taskobj setValue:[NSNumber numberWithBool:YES] forKey:@"upToDate"];	
		[taskobj setValue:[NSNumber numberWithBool:YES] forKey:@"existsOnServer"];	
		
		MQTaskPropertiesRequest *req = [[MQTaskPropertiesRequest alloc] initWithServer:server forTask:taskobj];
		[req addToQueue];
		[req autorelease];
		
		task_count++;
	}

	// delete stale tasks
	NSManagedObjectContext *context = [[NSApp delegate] managedObjectContext];
	taskEnum = [[[team valueForKey:@"tasks"] allObjects] objectEnumerator];
	BOOL deleted = NO;
	while (task = [taskEnum nextObject]) {
		if (![[task valueForKey:@"upToDate"] boolValue]) {
//			NSLog(@"task is stale: %@", [task valueForKey:@"title"]);
			[task setValue:nil forKey:@"server"];
			[task setValue:nil forKey:@"team"];
			[context deleteObject:task];		
			deleted = YES;
		}
	}
	if (deleted)
		[[NSApp delegate] performSelectorOnMainThread:@selector(reloadTasks) withObject:nil waitUntilDone:NO]; 
	
	// send a growl notification
	[GrowlApplicationBridge notifyWithTitle:@"Tasks Updated"
								description:[NSString stringWithFormat:@"Team %@: %d Tasks", [team valueForKey:@"name"], task_count]
						   notificationName:GROWL_TASKS_UPDATED
								   iconData:nil
								   priority:0
								   isSticky:NO
							   clickContext:GROWL_TASKS_UPDATED];
}

@end
