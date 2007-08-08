//
//  RequestController+Toolbar.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 19.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "RequestController+Toolbar.h"


@implementation RequestController (Toolbar)

- (void)initToolbars
{
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
}

- (IBAction)selectMode:(id)sender
{
	int tag = [sender tag];
	mode = tag;
//	NSLog(@"mode: %d", tag);
	
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
		if (forcedInspectorOut) {
			[inspectorWindow orderFront:self];
			forcedInspectorOut = NO;
		}
	}
	else if (tag == 1) {
		[window setToolbar:workspaceToolbar];
		[workspaceToolbar setSelectedItemIdentifier:@"MQFiles"];
		forcedInspectorOut = [inspectorWindow isVisible];
		[inspectorWindow orderOut:sender];
	}
}

#pragma mark - toolbar delegates

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
		[item setLabel:@"Info"];
		[item setPaletteLabel:@"Task Info"];
		[item setImage:[NSImage imageNamed:@"info"]];
		[item setTarget:self];
		[item setAction:@selector(toggleInspector:)];
	}
	else if ([itemIdentifier isEqualToString:@"MQRefresh"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQRefresh"];
		[item setLabel:@"Refresh View"];
		[item setPaletteLabel:@"Refresh View"];
		[item setImage:[NSImage imageNamed:@"refresh"]];
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
		[item setLabel:@"Synchronize"];
		[item setPaletteLabel:@"Synchronize"];
		[item setImage:[NSImage imageNamed:@"synchronize-vertical"]];
		[item setTarget:self];
		[item setTag:0];
		[item setAction:@selector(commitFiles:)];
	}
	else if ([itemIdentifier isEqualToString:@"MQDown"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQDown"];
		[item setLabel:@"Download"];
		[item setPaletteLabel:@"Download"];
		[item setImage:[NSImage imageNamed:@"sync-down"]];
		[item setTarget:self];
		[item setTag:1];
		[item setAction:@selector(commitFiles:)];
	}
	else if ([itemIdentifier isEqualToString:@"MQUp"]) {
		item = [[NSToolbarItem alloc] initWithItemIdentifier:@"MQUp"];
		[item setLabel:@"Upload"];
		[item setPaletteLabel:@"Upload"];
		[item setImage:[NSImage imageNamed:@"sync-up"]];
		[item setTarget:self];
		[item setTag:2];
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
		[items addObject:@"MQInfo"];
	}
	else if ([[_toolbar identifier] isEqualToString:FILES_TOOLBAR_ID]) {
//		[items addObject:@"MQDown"];
//		[items addObject:@"MQUp"];
		[items addObject:@"MQCommit"];
	}
	
	[items addObject:NSToolbarFlexibleSpaceItemIdentifier];
	[items addObject:@"MQRefresh"];
	[items addObject:@"MQStop"];
	
	[items addObject:NSToolbarSeparatorItemIdentifier];
	[items addObject:@"MQServer"];
	
    return items; 
}

- (NSArray *)toolbarSelectableItemIdentifiers:(NSToolbar*)_toolbar {
    return [NSArray arrayWithObjects:@"MQTasks", @"MQFiles", nil];
}


@end
