//
//  RequestController.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "RequestController.h"
#import "RequestController+Toolbar.h"

#import "MQTeamsRequest.h"
#import "MQTaskCell.h"
#import "MQUpdateRequest.h"
#import "MQTask.h"
#import "MQServer.h"
#import "LRFilterBar.h"
#import "StatusTransformer.h"
#import "MQTeam.h"
#import "MQChangeCell.h"
#import "MQSVNUpdateJob.h"
#import "MQSVNCommitJob.h"
#import "MQChange.h"
#import "PathAbbreviation.h"

@implementation RequestController

+ (void)initialize {
	[[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithInt:1], @"sortBy", [NSNumber numberWithInt:0], @"filesSortBy", nil]];
	[NSValueTransformer setValueTransformer:[[[StatusTransformer alloc] init] autorelease] forName:@"StatusTransformer"];
	[NSValueTransformer setValueTransformer:[[[PathAbbreviation alloc] init] autorelease] forName:@"PathAbbreviation"];
}

- (void)awakeFromNib
{	
	[serversController fetchWithRequest:nil merge:NO error:nil];
	[serversController setSelectionIndex:[[NSUserDefaults standardUserDefaults] integerForKey:@"selectedServer"]];
	
	[tasksController bind:@"contentSet" toObject:serversController withKeyPath:@"selection.tasks" options:nil];
	[tasksController fetchWithRequest:nil merge:NO error:nil];

	[changesController bind:@"contentSet" toObject:serversController withKeyPath:@"selection.changes" options:[NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES], NSDeletesObjectsOnRemoveBindingsOption, nil]];
	[changesController fetchWithRequest:nil merge:NO error:nil];
	
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
	
	[self initToolbars];
	
	NSArray *labels = [[[NSArray alloc] initWithObjects:@"LABEL:Sort by:", @"Title", @"Due Date", @"Status", @"Priority", @"Team", NULL] autorelease];
	[filterBar addItemsWithTitles:labels withSelector:@selector(titlebarSelectionChanged:) withSender:self];
	[filterBar selectTag:[[NSUserDefaults standardUserDefaults] integerForKey:@"sortBy"]];
	[self titlebarSelectionChanged:nil];
	
	labels = [[[NSArray alloc] initWithObjects:@"LABEL:Sort by:", @"Filename", @"Size", @"Kind", @"State", @"Team", NULL] autorelease];
	[changesFilterbar addItemsWithTitles:labels withSelector:@selector(fileSortSelectionChanged:) withSender:self];
	[changesFilterbar selectTag:[[NSUserDefaults standardUserDefaults] integerForKey:@"filesSortBy"]];
	[self fileSortSelectionChanged:nil];
	
	[self bind:@"teamList" toObject:teamsController withKeyPath:@"arrangedObjects.name" options:nil];
	
	[tasksTeamSelector bind:@"selectedIndex" toObject:self withKeyPath:@"tasksTeamSelection" options:nil];
	[filesTeamSelector bind:@"selectedIndex" toObject:self withKeyPath:@"filesTeamSelection" options:nil];
	
	[taskTable setTarget:self];
	[taskTable setDoubleAction:@selector(taskDoubleClick:)];

	[workspaceTable setTarget:self];
	[workspaceTable setDoubleAction:@selector(fileDoubleClick:)];
	
	if ([[serversController arrangedObjects] count] == 0) {
		[serversController add:nil];
		[serverDrawer toggle:nil];
	}
	
	[self performSelector:@selector(afterWakeFromNib) withObject:nil afterDelay:0.5];	
	
	[window makeKeyAndOrderFront:self];	
}

- (void)afterWakeFromNib
{
	[MQTask setAutoSaveEnabled:YES];
	
	[NSTimer scheduledTimerWithTimeInterval:600 target:self selector:@selector(backgroundRefresh) userInfo:nil repeats:YES];
	
	[self performSelector:@selector(backgroundRefresh) withObject:nil afterDelay:3];
}

- (void)dealloc
{
	[tasksToolbar release];
	[workspaceToolbar release];
	
	[tasksStringFilter release];
	[tasksTeamFilter release];
	[filesStringFilter release];
	[filesTeamFilter release];
	[oldTeamsList release];
	
	[super dealloc];
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
	else if (tag == 4)
		key = @"team.name";
	
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

- (void)fileSortSelectionChanged:(id)sender
{
	int tag;
	if (sender)
		tag = [sender tag];
	else
		tag = [[NSUserDefaults standardUserDefaults] integerForKey:@"filesSortBy"];
	
	[[NSUserDefaults standardUserDefaults] setInteger:tag forKey:@"filesSortBy"];
	
	NSString *key = nil;
	if (tag == 0)
		key = @"relPath";
	else if (tag == 1)
		key = @"fileSize";
	else if (tag == 2)
		key = @"fileKind";
	else if (tag == 3)
		key = @"status";
	else if (tag == 4)
		key = @"team.name";
	
	if (!key)
		return;
	
	NSMutableArray *desc = [[changesController sortDescriptors] mutableCopy];
	
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
	
	[desc insertObject:[[[NSSortDescriptor alloc] initWithKey:key ascending:YES] autorelease] atIndex:0];
	
	[changesController setSortDescriptors:desc];
	[desc release];
	
	[workspaceTable reloadData];
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

- (void)refreshWorkspaceWithUpdate:(BOOL)update 
{
	[[[[MQSVNUpdateJob alloc] initWithServer:[self selectedServer] updates:update] autorelease] addToQueue];
}

- (void)refreshTasks
{
	[[[[MQTeamsRequest alloc] initWithServer:[self selectedServer]] autorelease] addToQueue];
}

- (void)backgroundRefresh
{
	[self refreshTasks];
	[self refreshWorkspaceWithUpdate:NO];
}

- (IBAction)refresh:(id)sender
{
	if (mode == 0)
		[self refreshTasks];
	else if (mode == 1)
		[self refreshWorkspaceWithUpdate:NO];
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

- (IBAction)commitFiles:(id)sender
{
	int tag = [sender tag];

	if (tag == 0 || tag == 1) {
		// down
		[[[[MQSVNUpdateJob alloc] initWithServer:[self selectedServer] updates:YES] autorelease] addToQueue];
	}
	
	if (tag == 0 || tag == 2) {
		// up
		[[[[MQSVNCommitJob alloc] initWithServer:[self selectedServer]] autorelease] addToQueue];
	}
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
    else if ([insertedEntities containsObject:@"Change"] ||
			 [updatedEntities containsObject:@"Change"] ||
			 [deletedEntities containsObject:@"Change"])
    {
		[changesController performSelectorOnMainThread:@selector(rearrangeObjects) withObject:nil waitUntilDone:YES];
    }
}

- (IBAction)setServerLocalPath:(id)sender
{
	[passwordField validateEditing];
	
	NSOpenPanel *panel = [NSOpenPanel openPanel];
	[panel setCanChooseFiles:NO];
	[panel setCanChooseDirectories:YES];
	if ([panel runModal] == NSFileHandlingPanelOKButton && [[panel filenames] count] > 0) {
		NSString *dir = [[panel filenames] objectAtIndex:0];
		[[serversController selection] setValue:dir forKey:@"localPath"];
	}
}

- (IBAction)taskDoubleClick:(id)sender
{
	if ([[tasksController selectedObjects] count] == 0)
		return;
	id task = [[tasksController selectedObjects] objectAtIndex:0];
	
	[[NSWorkspace sharedWorkspace] openURL:[task webURL]];
}

- (IBAction)fileDoubleClick:(id)sender
{
	if ([[changesController selectedObjects] count] == 0)
		return;
	id file = [[changesController selectedObjects] objectAtIndex:0];

	[file revealInFinder];
}

#pragma mark - filter stuff

- (id)teamList
{
	return nil;
}

- (void)setTeamList:(id)list
{
	if (oldTeamsList && [list isEqualToArray:oldTeamsList])
		return;
	
//	NSLog(@"teams: %@", list);
	
	NSMenu *menu = [[NSMenu alloc] init];
	NSMenuItem *item = nil;
	
	item = [[[NSMenuItem alloc] init] autorelease];
	[item setTitle:@"All Teams"];
	[item setKeyEquivalent:@"0"];
	[item setKeyEquivalentModifierMask:NSCommandKeyMask];
	[menu addItem:item];
	
	[menu addItem:[NSMenuItem separatorItem]];
	
	int count = [list count];
	int i;
	for (i = 0; i < count; i++) {
		id name = [list objectAtIndex:i];
		if (![name isKindOfClass:[NSString class]])
			continue;
		
		item = [[[NSMenuItem alloc] init] autorelease];
		[item setTitle:name];
		
		if (i + 1 <= 9) {
			[item setKeyEquivalent:[NSString stringWithFormat:@"%d", i + 1]];
			[item setKeyEquivalentModifierMask:NSCommandKeyMask];
		}
		
		if (item)
			[menu addItem:item];
	}
	
	[tasksTeamSelector setMenu:menu];
	[self setTasksTeamSelection:0];
	
	[filesTeamSelector setMenu:[[menu copy] autorelease]];
	[self setFilesTeamSelection:0];
	
	[menu release];
	
	[oldTeamsList release];
	oldTeamsList = [list retain];
}

- (int)tasksTeamSelection
{
	return _tasksTeamSelection;
}

- (void)setTasksTeamSelection:(int)selection
{
	if (_tasksTeamSelection == selection)
		return;

	_tasksTeamSelection = selection;
	
	[tasksTeamFilter release];
	tasksTeamFilter = [[self filterForTeamSelectorPosition:selection] retain];
	[self setTasksFilter];}

- (int)filesTeamSelection
{
	return _filesTeamSelection;
}

- (void)setFilesTeamSelection:(int)selection
{
	if (_filesTeamSelection == selection)
		return;
	
	_filesTeamSelection = selection;
	
	[filesTeamFilter release];
	filesTeamFilter = [[self filterForTeamSelectorPosition:selection] retain];
	[self setFilesFilter];
}

- (NSPredicate *)filterForTeamSelectorPosition:(int)index
{
	if (index < 2)
		return nil;
	id team = [[teamsController arrangedObjects] objectAtIndex:index - 2];
	return [NSPredicate predicateWithFormat:[NSString stringWithFormat:@"team.id = \"%@\"", [team valueForKey:@"id"]]];
}

- (NSPredicate *)tasksStringFilter
{
	return tasksStringFilter;
}

- (void)setTasksStringFilter:(NSPredicate *)pred
{
	[tasksStringFilter release];
	tasksStringFilter = [pred retain];
	
	[self setTasksFilter];
}

- (void)setTasksFilter
{
	NSPredicate *pred = nil;
	if (tasksTeamFilter && tasksStringFilter)
		pred = [NSPredicate predicateWithFormat:[NSString stringWithFormat:@"(%@) and (%@)", [tasksTeamFilter predicateFormat], [tasksStringFilter predicateFormat]]];
	else if (tasksTeamFilter)
		pred = tasksTeamFilter;
	else if (tasksStringFilter)
		pred = tasksStringFilter;
		
	[tasksController setFilterPredicate:pred];
}

- (NSPredicate *)filesStringFilter
{
	return filesStringFilter;
}

- (void)setFilesStringFilter:(NSPredicate *)pred
{
	[filesStringFilter release];
	filesStringFilter = [pred retain];
	
	[self setFilesFilter];
}

- (void)setFilesFilter
{
	NSPredicate *pred = nil;
	if (filesTeamFilter && filesStringFilter)
		pred = [NSPredicate predicateWithFormat:[NSString stringWithFormat:@"(%@) and (%@)", [filesTeamFilter predicateFormat], [filesStringFilter predicateFormat]]];
	else if (filesTeamFilter)
		pred = filesTeamFilter;
	else if (filesStringFilter)
		pred = filesStringFilter;
		
	[changesController setFilterPredicate:pred];
}

- (IBAction)focusSearchField:(id)sender
{
	if (mode == 0)
		[tasksSearchField becomeFirstResponder];
	else if (mode == 1)
		[filesSearchField becomeFirstResponder];
}

@end
