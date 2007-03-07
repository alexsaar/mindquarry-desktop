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
#import "MQUpdateRequest.h"
#import "MQTask.h"

@implementation RequestController

- (void)awakeFromNib
{
	[serversController fetchWithRequest:nil merge:NO error:nil];
	[tasksController fetchWithRequest:nil merge:NO error:nil];
	
	MQTaskCell *cell = [[[MQTaskCell alloc] init] autorelease];
	[taskColumn setDataCell:cell];

#define MENU_ICON_SIZE NSMakeSize(16, 16)
	
#define ADD_MENU_ITEM(menu, title, icon_name) 	item = [[NSMenuItem alloc] init]; \
[item setTitle:title]; \
[item setImage:MQSmoothResize([NSImage imageNamed:icon_name], MENU_ICON_SIZE)]; \
[menu addItem:item]; \
[item release];

	NSMenuItem *item = nil;

	NSMenu *menu = [[NSMenu alloc] init];
	ADD_MENU_ITEM(menu, @"New", @"task-new");
	ADD_MENU_ITEM(menu, @"Running", @"task-running");
	ADD_MENU_ITEM(menu, @"Paused", @"task-paused");
	ADD_MENU_ITEM(menu, @"Done", @"task-done");
	[statusButton setMenu:menu];
	[menu release];
	
	menu = [[NSMenu alloc] init];
	ADD_MENU_ITEM(menu, @"Low", @"task-low");
	ADD_MENU_ITEM(menu, @"Medium", @"task-medium");
	ADD_MENU_ITEM(menu, @"Important", @"task-important");
	ADD_MENU_ITEM(menu, @"Critical", @"task-critical");
	[priorityButton setMenu:menu];
	[menu release];
	
	NSToolbar *toolbar = [[NSToolbar alloc] initWithIdentifier:@"MQDesktopMainToolbar"];
	[toolbar setDelegate:self];
	[toolbar setAllowsUserCustomization:YES];
	[toolbar setAutosavesConfiguration:YES];
	
	[window setToolbar:toolbar];
	[toolbar release];

	// TODO
//	[taskTable setTarget:inspectorWindow];
//	[taskTable setAction:@selector(makeKeyAndOrderFront:)];
//	[taskTable setDoubleAction:@selector(makeKeyAndOrderFront:)];
	
	if ([[serversController arrangedObjects] count] == 0) {
		[serversController add:nil];
		[serverDrawer toggle:nil];
	}
	
	[self performSelector:@selector(afterWakeFromNib) withObject:nil afterDelay:0.5];	
	
//	[self afterWakeFromNib];
	
}

- (void)afterWakeFromNib
{
//	[self refresh:nil];
	
	[MQTask setAutoSaveEnabled:YES];
}

- (NSToolbarItem *)toolbar:(NSToolbar *)toolbar itemForItemIdentifier:(NSString *)itemIdentifier willBeInsertedIntoToolbar:(BOOL)flag {
	
	NSToolbarItem *item = nil;
	
	if ([itemIdentifier isEqualToString:@"MQDone"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQDone"];
		[item setLabel:@"Mark Done"];
		[item setPaletteLabel:@"Mark Done"];
		[item setImage:[NSImage imageNamed:@"task-done"]];
		[item setTarget:self];
		[item setAction:@selector(setDone:)];
	}
	else if ([itemIdentifier isEqualToString:@"MQInfo"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQInfo"];
		[item setLabel:@"Inspector"];
		[item setPaletteLabel:@"Inspector"];
		[item setImage:[NSImage imageNamed:@"info"]];
		[item setTarget:self];
		[item setAction:@selector(toggleInspector:)];
	}
	else if  ([itemIdentifier isEqualToString:@"MQRefresh"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQRefresh"];
		[item setLabel:@"Reload"];
		[item setPaletteLabel:@"Reload"];
		[item setImage:[NSImage imageNamed:@"synchronize-vertical"]];
		[item setTarget:self];
		[item setAction:@selector(refresh:)];
	}
	else if  ([itemIdentifier isEqualToString:@"MQSave"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQSave"];
		[item setLabel:@"Save Task"];
		[item setPaletteLabel:@"Save Task"];
		[item setImage:[NSImage imageNamed:@"task-edit"]];
		[item setTarget:self];
		[item setAction:@selector(saveTask:)];
	}
	else if  ([itemIdentifier isEqualToString:@"MQServer"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQServer"];
		[item setLabel:@"Servers"];
		[item setPaletteLabel:@"Servers"];
		[item setImage:[NSImage imageNamed:@"servers"]];
		[item setTarget:serverDrawer];
		[item setAction:@selector(toggle:)];
	}
	
	return [item autorelease];
}

- (NSArray *)toolbarDefaultItemIdentifiers:(NSToolbar*)_toolbar {
    return [self toolbarAllowedItemIdentifiers:_toolbar];
}

- (NSArray *)toolbarAllowedItemIdentifiers:(NSToolbar*)_toolbar {
    return [NSArray arrayWithObjects:@"MQRefresh", @"MQDone", NSToolbarFlexibleSpaceItemIdentifier, @"MQServer", @"MQInfo", nil]; 
}

- (NSArray *)toolbarSelectableItemIdentifiers:(NSToolbar*)_toolbar {
    return nil;
}


- (IBAction)toggleInspector:(id)sender
{
	if ([inspectorWindow isVisible])
		[inspectorWindow orderOut:sender];
	else
		[inspectorWindow makeKeyAndOrderFront:sender];
}

- (IBAction)setDone:(id)sender
{
	[[tasksController selection] setValue:[NSNumber numberWithInt:3] forKey:@"statusIndex"];
}

- (IBAction)refresh:(id)sender
{
	id currentServer = [self selectedServer];	
	
	MQTeamsRequest *request = [[MQTeamsRequest alloc] initWithController:self forServer:currentServer];
	[request startRequest];
	[request autorelease];
}

- (IBAction)saveTask:(id)sender
{
	NSArray *tasks = [tasksController selectedObjects];
	if ([tasks count] == 0)
		return;
	
	[[tasks objectAtIndex:0] save];
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

	NSManagedObject *task = [[MQTask alloc] initWithEntity:entity insertIntoManagedObjectContext:context];
	[task setValue:task_id forKey:@"id"];
	[task setValue:team forKey:@"team"];
	
	return task;
}

@end
