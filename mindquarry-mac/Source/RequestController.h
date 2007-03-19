//
//  RequestController.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

enum {
	MQRequestTeams
};

@interface RequestController : NSObject {

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
	
	id tasksToolbar;
	
	id workspaceToolbar;
	
	int mode;
	
	IBOutlet id changesFilterbar;

	IBOutlet id workspaceTable;
	
}

- (void)afterWakeFromNib;

- (void)titlebarSelectionChanged:(id)sender;

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

@end
