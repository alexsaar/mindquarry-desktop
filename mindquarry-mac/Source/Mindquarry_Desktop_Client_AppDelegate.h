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
	
	IBOutlet id statusSpinner;
	IBOutlet id statusField;
	
	IBOutlet id serverController;
	
	IBOutlet id taskController;
	IBOutlet id taskTable;
	
	id refreshToolbarItem;
    id stopToolbarItem;
	
    NSPersistentStoreCoordinator *persistentStoreCoordinator;
    NSManagedObjectModel *managedObjectModel;
    NSManagedObjectContext *managedObjectContext;
}

- (NSPersistentStoreCoordinator *)persistentStoreCoordinator;
- (NSManagedObjectModel *)managedObjectModel;
- (NSManagedObjectContext *)managedObjectContext;

- (IBAction)saveAction:sender;

- (void)reloadTasks;

@end
