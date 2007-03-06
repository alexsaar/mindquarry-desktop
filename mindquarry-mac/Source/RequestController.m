//
//  RequestController.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "RequestController.h"

#import "MQTeamsRequest.h"
#import "MQTaskCell.h"

@implementation RequestController

- (void)awakeFromNib
{
//	[[dateColumn dataCell] setFormatter:dateFormatter];
	
	MQTaskCell *cell = [[[MQTaskCell alloc] init] autorelease];
	[taskColumn setDataCell:cell];
	
}

- (IBAction)test:(id)sender
{
	id currentServer = [self selectedServer];	
	
	MQTeamsRequest *request = [[MQTeamsRequest alloc] initWithController:self forServer:currentServer];
	[request startRequest];
		
}

- (id)selectedServer
{
	NSArray *servers = [serversController selectedObjects];
	if ([servers count] > 0)
		return [servers objectAtIndex:0];
	return nil;
}



- (id)teamWithId:(NSString *)team_id forServer:(id)server
{
	NSArray *teams = [[server valueForKey:@"teams"] allObjects];
	teams = [teams filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:[NSString stringWithFormat:@"id = \"%@\"", team_id]]];
	
	if ([teams count] > 0)
		return [teams objectAtIndex:0];
	
	NSManagedObjectContext *context = [[NSApp delegate] managedObjectContext];
	NSEntityDescription *entity = [[[[NSApp delegate] managedObjectModel] entitiesByName] objectForKey:@"Team"];
	
	NSManagedObject *team = [[NSManagedObject alloc] initWithEntity:entity insertIntoManagedObjectContext:context];
	[team setValue:team_id forKey:@"id"];
	[team setValue:server forKey:@"server"];
	
	return team;
}

- (id)taskWithId:(NSString *)task_id forTeam:(id)team
{
	NSArray *tasks = [[team valueForKey:@"tasks"] allObjects];
	tasks = [tasks filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:[NSString stringWithFormat:@"id = \"%@\"", task_id]]];
	
	if ([tasks count] > 0)
		return [tasks objectAtIndex:0];
	
	NSManagedObjectContext *context = [[NSApp delegate] managedObjectContext];
	NSEntityDescription *entity = [[[[NSApp delegate] managedObjectModel] entitiesByName] objectForKey:@"Task"];

	NSManagedObject *task = [[NSManagedObject alloc] initWithEntity:entity insertIntoManagedObjectContext:context];
	[task setValue:task_id forKey:@"id"];
	[task setValue:team forKey:@"team"];
	
	return task;
}

@end
