//
//  Mindquarry_Desktop_Client_AppDelegate.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright __MyCompanyName__ 2007 . All rights reserved.
//

#import <Cocoa/Cocoa.h>

@interface Mindquarry_Desktop_Client_AppDelegate : NSObject 
{
    IBOutlet NSWindow *window;
	
	IBOutlet id controller;
	
	IBOutlet id statusSpinner;
	IBOutlet id statusField;
	
	IBOutlet id serverController;
	
	IBOutlet id taskController;
	IBOutlet id taskTable;
	
	IBOutlet id changesController;
	IBOutlet id changesTable;
	
	IBOutlet id serverUsernameField;
	IBOutlet id serverPasswordField;
	
	id refreshToolbarItem;
    id stopToolbarItem;
	id commitFilesToolbarItem;
	id commitTasksToolbarItem;
	
	IBOutlet id commitMessageWindow;
	IBOutlet id commitMessageField;
	
	IBOutlet id progressController;
	IBOutlet id progressWindow;
	
	NSString *cachedMessage;
	
    NSPersistentStoreCoordinator *persistentStoreCoordinator;
    NSManagedObjectModel *managedObjectModel;
    NSManagedObjectContext *managedObjectContext;
}

- (NSPersistentStoreCoordinator *)persistentStoreCoordinator;
- (NSManagedObjectModel *)managedObjectModel;
- (NSManagedObjectContext *)managedObjectContext;

- (IBAction)saveAction:sender;

- (NSString *)getCommitMessage;
- (IBAction)commit:(id)sender;

- (void)reloadTasks;

- (void)setProgressVisible:(NSNumber *)visible;
- (void)_closeProgressVisible;
- (void)addProgressPath:(NSString *)path withAction:(NSString *)action;

@end
