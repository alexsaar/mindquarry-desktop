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

@implementation MQTasksRequest

- (id)initWithController:(RequestController *)_controller forServer:(id)_server forTeam:(id)_team;
{
	if (![super initWithController:_controller forServer:_server])
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
		
		id taskobj = [controller taskWithId:obj_id forTeam:team];
		[taskobj setValue:[NSNumber numberWithBool:YES] forKey:@"upToDate"];	
		
		MQTaskPropertiesRequest *req = [[MQTaskPropertiesRequest alloc] initWithController:controller forServer:server forTask:taskobj];
		[req addToQueue];
		[req autorelease];
	}

	// delete stale tasks
	NSManagedObjectContext *context = [[NSApp delegate] managedObjectContext];
	taskEnum = [[[team valueForKey:@"tasks"] allObjects] objectEnumerator];
	task;
	while (task = [taskEnum nextObject]) {
		if (![[task valueForKey:@"upToDate"] boolValue]) {
//			NSLog(@"stale %@", [task valueForKey:@"title"]);
			[context deleteObject:task];		
		}
	}	
	[[NSApp delegate] performSelectorOnMainThread:@selector(reloadTasks) withObject:nil waitUntilDone:NO]; 
//	NSLog(@"refreshed");
	
}

@end
