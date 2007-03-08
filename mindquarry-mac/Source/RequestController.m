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
#import "MQServer.h"
#import "LRFilterBar.h"

@implementation RequestController

+ (void)initialize {
	[[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithInt:1], @"sortBy", nil]];
}

- (void)awakeFromNib
{
	
	[serversController fetchWithRequest:nil merge:NO error:nil];
	[serversController setSelectionIndex:[[NSUserDefaults standardUserDefaults] integerForKey:@"selectedServer"]];
	
	[tasksController bind:@"contentSet" toObject:serversController withKeyPath:@"selection.tasks" options:nil];
	[tasksController fetchWithRequest:nil merge:NO error:nil];

	[serversController willChangeValueForKey:@"selection"];
	[serversController didChangeValueForKey:@"selection"];
	
	[taskTable reloadData];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(objectsDidChange:) name:NSManagedObjectContextObjectsDidChangeNotification object:[[NSApp delegate] managedObjectContext]];
	
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
	
	NSToolbar *toolbar = [[NSToolbar alloc] initWithIdentifier:@"MQDesktopMainToolbar2"];
	[toolbar setDelegate:self];
	[toolbar setAllowsUserCustomization:YES];
	[toolbar setAutosavesConfiguration:YES];
	
	[window setToolbar:toolbar];
	[toolbar release];
	
	NSArray *labels = [[[NSArray alloc] initWithObjects:@"LABEL:Sort by:", @"Title", @"Due Date", @"Status", @"Priority", NULL] autorelease];
	[filterBar addItemsWithTitles:labels withSelector:@selector(titlebarSelectionChanged:) withSender:self];
	[filterBar selectTag:[[NSUserDefaults standardUserDefaults] integerForKey:@"sortBy"]];
	[self titlebarSelectionChanged:nil];
	
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

	[window makeKeyAndOrderFront:self];
	
}

- (void)afterWakeFromNib
{

//	[self refresh:nil];
	
	[MQTask setAutoSaveEnabled:YES];
	
	[NSTimer scheduledTimerWithTimeInterval:120 target:self selector:@selector(refresh:) userInfo:nil repeats:YES];
	[self performSelector:@selector(refresh:) withObject:nil afterDelay:3];
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
		[item setAutovalidates:NO];
		[[NSApp delegate] setValue:item forKey:@"refreshToolbarItem"];
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
	else if ([itemIdentifier isEqualToString:@"MQStop"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQStop"];
		[item setLabel:@"Stop"];
		[item setPaletteLabel:@"Stop"];
		[item setImage:[NSImage imageNamed:@"AlertStopIcon"]];
		[item setTarget:self];
		[item setAction:@selector(stopTasks:)];
		[item setAutovalidates:NO];
		[item setEnabled:NO];

		[[NSApp delegate] setValue:item forKey:@"stopToolbarItem"];
	}
	
	return [item autorelease];
}

- (NSArray *)toolbarDefaultItemIdentifiers:(NSToolbar*)_toolbar {
    return [self toolbarAllowedItemIdentifiers:_toolbar];
}

- (NSArray *)toolbarAllowedItemIdentifiers:(NSToolbar*)_toolbar {
    return [NSArray arrayWithObjects:@"MQRefresh", @"MQStop", @"MQDone", NSToolbarFlexibleSpaceItemIdentifier, @"MQServer", @"MQInfo", nil]; 
}

- (NSArray *)toolbarSelectableItemIdentifiers:(NSToolbar*)_toolbar {
    return nil;
}

- (void)titlebarSelectionChanged:(id)sender
{
	int tag;
	if (sender)
		tag = [sender tag];
	else
		tag = [[NSUserDefaults standardUserDefaults] integerForKey:@"sortBy"];
	
	[[NSUserDefaults standardUserDefaults] setInteger:tag forKey:@"sortBy"];
	
//	NSLog(@"change %@ %d", sender, tag);
	
	NSString *key = nil;
	if (tag == 0)
		key = @"title";
	else if (tag == 1)
		key = @"sortDate";
	else if (tag == 2)
		key = @"statusIndex";
	else if (tag == 3)
		key = @"priorityIndex";
	
	if (!key)
		return;
	
	NSMutableArray *desc = [[tasksController sortDescriptors] mutableCopy];
	
	int i;
	int count = [desc count];
	id remove = nil;
	for (i = 0; i < count; i++) {
		NSSortDescriptor *sd = [desc objectAtIndex:i];
		if ([[sd key] isEqualToString:key])
			remove = sd;
	}
	if (remove)
		[desc removeObject:remove];
	
	[desc insertObject:[[[NSSortDescriptor alloc] initWithKey:key ascending:tag != 3] autorelease] atIndex:0];
	
	[tasksController setSortDescriptors:desc];
	[desc release];
	
	[taskTable reloadData];
	
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
	
//	NSLog(@"refresh %@", currentServer);
	
	MQTeamsRequest *request = [[MQTeamsRequest alloc] initWithController:self forServer:currentServer];
	[request addToQueue];
	[request autorelease];
}

- (IBAction)stopTasks:(id)sender
{
	NSEnumerator *servers = [[serversController arrangedObjects] objectEnumerator];
	id server;
	while (server = [servers nextObject]) {
		[server cancelAll];
	}
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
	[task setValue:[team valueForKey:@"server"] forKey:@"server"];
	
	return task;
}

- (void)objectsDidChange:(NSNotification *)note
{
    NSArray *insertedEntities = [[[note userInfo] valueForKey:NSInsertedObjectsKey] valueForKeyPath:@"entity.name"];
    NSArray *updatedEntities  = [[[note userInfo] valueForKey:NSUpdatedObjectsKey] valueForKeyPath:@"entity.name"];
    NSArray *deletedEntities  = [[[note userInfo] valueForKey:NSDeletedObjectsKey] valueForKeyPath:@"entity.name"];
    
//	NSLog(@"change");
	
    // Use whatever entity name, or use an NSEntityDescription and key path @"entity" above
    if ([insertedEntities containsObject:@"Task"] ||
        [updatedEntities containsObject:@"Task"] ||
        [deletedEntities containsObject:@"Task"])
    {
        [tasksController performSelectorOnMainThread:@selector(rearrangeObjects) withObject:nil waitUntilDone:YES];
		[taskTable performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:YES];
    }
    else if ([insertedEntities containsObject:@"Server"] ||
			 [updatedEntities containsObject:@"Server"] ||
			 [deletedEntities containsObject:@"Server"])
    {
		[serversController performSelectorOnMainThread:@selector(rearrangeObjects) withObject:nil waitUntilDone:YES];
    }
//    else if ([insertedEntities containsObject:@"Action"] ||
//             [updatedEntities containsObject:@"Action"] ||
//             [deletedEntities containsObject:@"Action"])
//    {
//        [actionController rearrangeObjects];
//    }
}

@end
