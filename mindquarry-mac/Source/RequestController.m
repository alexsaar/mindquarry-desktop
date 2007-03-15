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
#import "StatusTransformer.h"
#import "IconTransformer.h"
#import "StatusColorTransformer.h"
#import "MQTeam.h"
#import "MQChangeCell.h"
#import "MQSVNUpdateJob.h"

#define TASKS_TOOLBAR_ID @"MQDesktopMainToolbar2"
#define FILES_TOOLBAR_ID @"MQDesktopWorkToolbar2"

@implementation RequestController

+ (void)initialize {
	[[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithInt:1], @"sortBy", nil]];
}

- (void)awakeFromNib
{
	[NSValueTransformer setValueTransformer:[[[StatusTransformer alloc] init] autorelease] forName:@"StatusTransformer"];
	[NSValueTransformer setValueTransformer:[[[IconTransformer alloc] init] autorelease] forName:@"IconTransformer"];
	[NSValueTransformer setValueTransformer:[[[StatusColorTransformer alloc] init] autorelease] forName:@"StatusColorTransformer"];	
	
	[serversController fetchWithRequest:nil merge:NO error:nil];
	[serversController setSelectionIndex:[[NSUserDefaults standardUserDefaults] integerForKey:@"selectedServer"]];
	
	[tasksController bind:@"contentSet" toObject:serversController withKeyPath:@"selection.tasks" options:nil];
	[tasksController fetchWithRequest:nil merge:NO error:nil];

	[serversController willChangeValueForKey:@"selection"];
	[serversController didChangeValueForKey:@"selection"];
	
	[taskTable reloadData];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(objectsDidChange:) name:NSManagedObjectContextObjectsDidChangeNotification object:[[NSApp delegate] managedObjectContext]];
	
	[taskColumn setDataCell:[[[MQTaskCell alloc] init] autorelease]];
	
	[changeColumn setDataCell:[[[MQChangeCell alloc] init] autorelease]];

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
	
	tasksToolbar = [[NSToolbar alloc] initWithIdentifier:TASKS_TOOLBAR_ID];
	[tasksToolbar setDelegate:self];
	[tasksToolbar setAllowsUserCustomization:YES];
	[tasksToolbar setAutosavesConfiguration:YES];
	[tasksToolbar setSelectedItemIdentifier:@"MQTasks"];
	[window setToolbar:tasksToolbar];
	
	workspaceToolbar = [[NSToolbar alloc] initWithIdentifier:FILES_TOOLBAR_ID];
	[workspaceToolbar setDelegate:self];
	[workspaceToolbar setAllowsUserCustomization:YES];
	[workspaceToolbar setAutosavesConfiguration:YES];
	[workspaceToolbar setSelectedItemIdentifier:@"MQFiles"];
		
	[self selectMode:nil];
	
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

- (void)dealloc
{
	[tasksToolbar release];
	[workspaceToolbar release];
	
	[super dealloc];
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
	else if ([itemIdentifier isEqualToString:@"MQRefresh"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQRefresh"];
		[item setLabel:@"Reload"];
		[item setPaletteLabel:@"Reload"];
		[item setImage:[NSImage imageNamed:@"synchronize-vertical"]];
		[item setTarget:self];
		[item setAction:@selector(refresh:)];
		[item setAutovalidates:NO];
		[[NSApp delegate] setValue:item forKey:@"refreshToolbarItem"];
	}
	else if ([itemIdentifier isEqualToString:@"MQSave"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQSave"];
		[item setLabel:@"Save Task"];
		[item setPaletteLabel:@"Save Task"];
		[item setImage:[NSImage imageNamed:@"task-edit"]];
		[item setTarget:self];
		[item setAction:@selector(saveTask:)];
	}
	else if ([itemIdentifier isEqualToString:@"MQServer"]) {
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
	else if ([itemIdentifier isEqualToString:@"MQCreateTask"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQCreateTask"];
		[item setLabel:@"New Task"];
		[item setPaletteLabel:@"New Task"];
		[item setImage:[NSImage imageNamed:@"task-add"]];
		[item setTarget:self];
		[item setAction:@selector(createTask:)];
	}
	else if ([itemIdentifier isEqualToString:@"MQTasks"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQTasks"];
		[item setLabel:@" Tasks    "];
		[item setPaletteLabel:@"Tasks"];
		[item setImage:[NSImage imageNamed:@"mindquarry-tasks"]];
		[item setTarget:self];
		[item setTag:0];
		[item setAction:@selector(selectMode:)];
	}
	else if ([itemIdentifier isEqualToString:@"MQFiles"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQFiles"];
		[item setLabel:@"Workspace"];
		[item setPaletteLabel:@"Workspace"];
		[item setImage:[NSImage imageNamed:@"mindquarry-documents"]];
		[item setTarget:self];
		[item setTag:1];
		[item setAction:@selector(selectMode:)];
	}
	else if ([itemIdentifier isEqualToString:@"MQCommit"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQCommit"];
		[item setLabel:@"Commit"];
		[item setPaletteLabel:@"Commit"];
//		[item setImage:[NSImage imageNamed:@"mindquarry-documents"]];
		[item setTarget:self];
		[item setAction:@selector(commitFiles:)];
	}
	
	return [item autorelease];
}

- (NSArray *)toolbarDefaultItemIdentifiers:(NSToolbar*)_toolbar {
    return [self toolbarAllowedItemIdentifiers:_toolbar];
}

- (NSArray *)toolbarAllowedItemIdentifiers:(NSToolbar*)_toolbar {
	NSMutableArray *items = [NSMutableArray arrayWithObjects:@"MQTasks", @"MQFiles", NSToolbarSeparatorItemIdentifier, nil];
	if ([[_toolbar identifier] isEqualToString:TASKS_TOOLBAR_ID]) {
		[items addObject:@"MQCreateTask"];
		[items addObject:@"MQDone"];
	}
	else if ([[_toolbar identifier] isEqualToString:FILES_TOOLBAR_ID]) {
		[items addObject:@"MQCommit"];
	}
	
	[items addObject:NSToolbarFlexibleSpaceItemIdentifier];
	[items addObject:@"MQRefresh"];
	[items addObject:@"MQStop"];
	
	[items addObject:NSToolbarSeparatorItemIdentifier];
	[items addObject:@"MQServer"];
	[items addObject:@"MQInfo"];
	
    return items; 
}

- (NSArray *)toolbarSelectableItemIdentifiers:(NSToolbar*)_toolbar {
    return [NSArray arrayWithObjects:@"MQTasks", @"MQFiles", nil];
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

	if (mode == 0) {
		MQTeamsRequest *request = [[MQTeamsRequest alloc] initWithServer:currentServer];
		[request addToQueue];
		[request autorelease];		
	}
	else if (mode == 1) {
		MQSVNUpdateJob *job = [[[MQSVNUpdateJob alloc] initWithServer:currentServer] autorelease];
		[job addToQueue];
	}
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

- (IBAction)createTask:(id)sender
{
	if (![createTaskSheet isVisible]) {
		[createTaskTitle setStringValue:@"New Task"];
		[NSApp beginSheet:createTaskSheet modalForWindow:window modalDelegate:self didEndSelector:nil contextInfo:nil];		
	}
}

- (IBAction)finishCreateTask:(id)sender
{
	[self cancelCreateTask:sender];
	
	int teamIndex = [createTaskTeamButton indexOfSelectedItem];
	id team = [[teamsController arrangedObjects] objectAtIndex:teamIndex];
	NSString *title = [createTaskTitle stringValue];
	
	id task = [tasksController newObject];
	[task setValue:team forKey:@"team"];
	[task setValue:[team valueForKey:@"server"] forKey:@"server"];
	[task setValue:title forKey:@"title"];
		
	[tasksController setSelectedObjects:[NSArray arrayWithObject:task]];
	
	[task performSelectorOnMainThread:@selector(save) withObject:nil waitUntilDone:NO];
	[task setAutoSaveEnabled:YES];
}

- (IBAction)cancelCreateTask:(id)sender
{
	[createTaskSheet orderOut:self];
    [NSApp endSheet:createTaskSheet];
}

- (IBAction)selectMode:(id)sender
{
	int tag = [sender tag];
	mode = tag;
	NSLog(@"mode: %d", tag);
	
	if ([[rootView subviews] count] > 0)
		[[[rootView subviews] objectAtIndex:0] removeFromSuperview];
	
	NSView *newView = nil;
	if (tag == 0) {
		newView = tasksView;
	}
	else if (tag == 1) {
		newView = workspaceView;
	}
	
	if (newView) {
		[newView setFrame:[rootView bounds]];
		[rootView addSubview:newView];		
	}
	
	if (tag == 0) {
		[window setToolbar:tasksToolbar];
		[tasksToolbar setSelectedItemIdentifier:@"MQTasks"];
	}
	else if (tag == 1) {
		[window setToolbar:workspaceToolbar];
		[workspaceToolbar setSelectedItemIdentifier:@"MQFiles"];
	}
}

- (IBAction)commitFiles:(id)sender
{
	
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
	
	MQTeam *team = [[MQTeam alloc] initWithEntity:entity insertIntoManagedObjectContext:context];
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

- (void)objectsDidChange:(NSNotification *)note
{
    NSArray *insertedEntities = [[[note userInfo] valueForKey:NSInsertedObjectsKey] valueForKeyPath:@"entity.name"];
    NSArray *updatedEntities  = [[[note userInfo] valueForKey:NSUpdatedObjectsKey] valueForKeyPath:@"entity.name"];
    NSArray *deletedEntities  = [[[note userInfo] valueForKey:NSDeletedObjectsKey] valueForKeyPath:@"entity.name"];
    	
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
}

@end
