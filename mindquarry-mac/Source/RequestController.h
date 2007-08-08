//
//  RequestController.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import <Growl/GrowlApplicationBridge.h>

enum {
	MQRequestTeams
};

@interface RequestController : NSObject <GrowlApplicationBridgeDelegate> {

	IBOutlet id taskColumn;
		
	IBOutlet id serversController;

	IBOutlet id tasksController;
	
	IBOutlet id teamsController;
	
	IBOutlet id statusButton;
	
	IBOutlet id priorityButton;

	IBOutlet id window;

	IBOutlet id inspectorWindow;
	
	IBOutlet id taskTable;
	
	IBOutlet id serverDrawer;
	
	IBOutlet id filterBar;
	
	IBOutlet id createTaskSheet;
	
	IBOutlet id createTaskTeamButton;
	
	IBOutlet id createTaskTitle;
	
	IBOutlet id rootView;

	IBOutlet id tasksView;
	
	IBOutlet id workspaceView;
	
	IBOutlet id changesController;
	
	IBOutlet id changeColumn;
	
	IBOutlet id changeEnabledColumn;
	
	id tasksToolbar;
	
	id workspaceToolbar;
	
	int mode;
	
	IBOutlet id changesFilterbar;

	IBOutlet id workspaceTable;
	
	IBOutlet id tasksTeamSelector;
	
	IBOutlet id filesTeamSelector;
	
	int _tasksTeamSelection;
	
	int _filesTeamSelection;
	
	NSPredicate *tasksStringFilter;
	
	NSPredicate *tasksTeamFilter;
	
	NSPredicate *filesStringFilter;
	
	NSPredicate *filesTeamFilter;
	
	NSArray *oldTeamsList;
	
	IBOutlet id tasksSearchField;
	
	IBOutlet id filesSearchField;
	
	IBOutlet id passwordField;
    
    BOOL hasUnsavedTasks;
	
	BOOL forcedInspectorOut;
	
}

- (void)afterWakeFromNib;

- (void)titlebarSelectionChanged:(id)sender;

- (void)fileSortSelectionChanged:(id)sender;

- (IBAction)toggleInspector:(id)sender;

- (IBAction)setDone:(id)sender;

- (void)refreshWorkspaceWithUpdate:(BOOL)update;

- (void)backgroundRefresh;

- (void)refreshTasks;

- (IBAction)refresh:(id)sender;

- (IBAction)stopTasks:(id)sender;

- (IBAction)saveTask:(id)sender;

- (IBAction)createTask:(id)sender;

- (IBAction)finishCreateTask:(id)sender;

- (IBAction)cancelCreateTask:(id)sender;

- (IBAction)commitFiles:(id)sender;

- (id)selectedServer;

- (id)teamWithId:(NSString *)team_id forServer:(id)server;

- (id)taskWithId:(NSString *)task_id forTeam:(id)team;

- (void)objectsDidChange:(NSNotification *)note;

- (IBAction)setServerLocalPath:(id)sender;

- (IBAction)taskDoubleClick:(id)sender;

- (IBAction)fileDoubleClick:(id)sender;

- (id)teamList;
- (void)setTeamList:(id)list;

- (int)tasksTeamSelection;
- (void)setTasksTeamSelection:(int)selection;

- (int)filesTeamSelection;
- (void)setFilesTeamSelection:(int)selection;

- (NSPredicate *)filterForTeamSelectorPosition:(int)index;

- (NSPredicate *)tasksStringFilter;
- (void)setTasksStringFilter:(NSPredicate *)pred;

- (void)setTasksFilter;

- (NSPredicate *)filesStringFilter;
- (void)setFilesStringFilter:(NSPredicate *)pred;

- (void)setFilesFilter;

- (IBAction)focusSearchField:(id)sender;

- (IBAction)saveUnsavedTasks:(id)sender;

// Growl

- (NSDictionary *)registrationDictionaryForGrowl;

- (NSString *)applicationNameForGrowl;

- (void)growlNotificationWasClicked:(id)clickContext;

@end
