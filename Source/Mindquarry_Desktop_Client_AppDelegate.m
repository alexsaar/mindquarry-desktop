//
//  Mindquarry_Desktop_Client_AppDelegate.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright __MyCompanyName__ 2007 . All rights reserved.
//

#import "Mindquarry_Desktop_Client_AppDelegate.h"
#import "MQServer.h"
#import "MQSVNJob.h"

@implementation Mindquarry_Desktop_Client_AppDelegate


/**
    Returns the support folder for the application, used to store the Core Data
    store file.  This code uses a folder named "Mindquarry_Desktop_Client" for
    the content, either in the NSApplicationSupportDirectory location or (if the
    former cannot be found), the system's temporary directory.
 */

- (NSString *)applicationSupportFolder {

    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, YES);
    NSString *basePath = ([paths count] > 0) ? [paths objectAtIndex:0] : NSTemporaryDirectory();
    return [basePath stringByAppendingPathComponent:@"Mindquarry Desktop Client"];
}


/**
    Creates, retains, and returns the managed object model for the application 
    by merging all of the models found in the application bundle and all of the 
    framework bundles.
 */
 
- (NSManagedObjectModel *)managedObjectModel {

    if (managedObjectModel != nil) {
        return managedObjectModel;
    }
	
    NSString *path = [[NSBundle mainBundle] pathForResource:@"Mindquarry_Desktop_Client_DataModel" ofType:@"mom"];
    NSURL *url = [NSURL fileURLWithPath:path];
    managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:url];
        
    return managedObjectModel;
}


/**
    Returns the persistent store coordinator for the application.  This 
    implementation will create and return a coordinator, having added the 
    store for the application to it.  (The folder for the store is created, 
    if necessary.)
 */

- (NSPersistentStoreCoordinator *) persistentStoreCoordinator {

    if (persistentStoreCoordinator != nil) {
        return persistentStoreCoordinator;
    }

    NSFileManager *fileManager;
    NSString *applicationSupportFolder = nil;
    NSError *error;
    
    fileManager = [NSFileManager defaultManager];
    applicationSupportFolder = [self applicationSupportFolder];
    if ( ![fileManager fileExistsAtPath:applicationSupportFolder isDirectory:NULL] ) {
        [fileManager createDirectoryAtPath:applicationSupportFolder attributes:nil];
    }
    
    persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel: [self managedObjectModel]];

    NSString *newPath = [applicationSupportFolder stringByAppendingPathComponent: @"PersistentStore.sqlite"];
	NSURL *newUrl = [NSURL fileURLWithPath:newPath];
    if (![persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:newUrl options:nil error:&error]){
        [[NSApplication sharedApplication] presentError:error];
    } 
    
    return persistentStoreCoordinator;
}


/**
    Returns the managed object context for the application (which is already
    bound to the persistent store coordinator for the application.) 
 */
 
- (NSManagedObjectContext *) managedObjectContext {

    if (managedObjectContext != nil) {
        return managedObjectContext;
    }

    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil) {
        managedObjectContext = [[NSManagedObjectContext alloc] init];
        [managedObjectContext setPersistentStoreCoordinator: coordinator];
    }
    
    return managedObjectContext;
}


/**
    Returns the NSUndoManager for the application.  In this case, the manager
    returned is that of the managed object context for the application.
 */
 
- (NSUndoManager *)windowWillReturnUndoManager:(NSWindow *)window {
    return [[self managedObjectContext] undoManager];
}


/**
    Performs the save action for the application, which is to send the save:
    message to the application's managed object context.  Any encountered errors
    are presented to the user.
 */
 
- (IBAction) saveAction:(id)sender {

    NSError *error = nil;
    if (![[self managedObjectContext] save:&error]) {
        [[NSApplication sharedApplication] presentError:error];
    }
}


/**
    Implementation of the applicationShouldTerminate: method, used here to
    handle the saving of changes in the application managed object context
    before the application terminates.
 */
 
- (NSApplicationTerminateReply)applicationShouldTerminate:(NSApplication *)sender {

    NSError *error;
    NSApplicationTerminateReply reply = NSTerminateNow;
    
//	NSEntityDescription *entity = [[managedObjectModel entitiesByName] objectForKey:@"Change"];
//	NSFetchRequest *req = [[[NSFetchRequest alloc] init] autorelease];
//	[req setEntity:entity];
//	NSArray *chan = [managedObjectContext executeFetchRequest:req error:nil];
//	NSEnumerator *chEnum = [chan objectEnumerator];
//	id ch;
//	while (ch = [chEnum nextObject]) {
//		[managedObjectContext deleteObject:ch];
//	}
	
    if (managedObjectContext != nil) {
        if ([managedObjectContext commitEditing]) {
            if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error]) {
				
                // This error handling simply presents error information in a panel with an 
                // "Ok" button, which does not include any attempt at error recovery (meaning, 
                // attempting to fix the error.)  As a result, this implementation will 
                // present the information to the user and then follow up with a panel asking 
                // if the user wishes to "Quit Anyway", without saving the changes.

                // Typically, this process should be altered to include application-specific 
                // recovery steps.  

                BOOL errorResult = [[NSApplication sharedApplication] presentError:error];
				
                if (errorResult == YES) {
                    reply = NSTerminateCancel;
                } 

                else {
					
                    int alertReturn = NSRunAlertPanel(nil, @"Could not save changes while quitting. Quit anyway?" , @"Quit anyway", @"Cancel", nil);
                    if (alertReturn == NSAlertAlternateReturn) {
                        reply = NSTerminateCancel;	
                    }
                }
            }
        } 
        
        else {
            reply = NSTerminateCancel;
        }
    }
    
    return reply;
}

- (void)applicationWillTerminate:(NSNotification *)aNotification
{
	[[NSUserDefaults standardUserDefaults] setInteger:[serverController selectionIndex] forKey:@"selectedServer"];
}

/**
    Implementation of dealloc, to release the retained variables.
 */
 
- (void) dealloc {

    [managedObjectContext release], managedObjectContext = nil;
    [persistentStoreCoordinator release], persistentStoreCoordinator = nil;
    [managedObjectModel release], managedObjectModel = nil;
    [super dealloc];
}

- (BOOL)applicationShouldHandleReopen:(NSApplication *)theApplication hasVisibleWindows:(BOOL)flag
{
	[window makeKeyAndOrderFront:nil];
	return YES;
}

- (NSString *)getCommitMessage
{
	NSString *message = [self valueForKey:@"cachedMessage"];
	if (message)
		return message;
	
	[commitMessageField setString:@"<message>"];
	[commitMessageField setSelectedRange:NSMakeRange(0, [[commitMessageField string] length])];
    [commitMessageWindow makeKeyAndOrderFront:self];
//	[NSApp beginSheet:commitMessageWindow modalForWindow:window modalDelegate:nil didEndSelector:nil contextInfo:nil];
	if ([NSApp runModalForWindow:commitMessageWindow] == NSRunAbortedResponse)  {
		message = nil;
        [MQSVNJob cancelCurrentJob];
    }
	else 
		message = [commitMessageField string];
	[self setValue:message forKey:@"cachedMessage"];
	return message;
}

- (IBAction)commit:(id)sender
{
	if ([sender tag] == 1) {
		[NSApp abortModal];
	}
	else {
		[NSApp stopModal];
	}
	[commitMessageWindow orderOut:self];
//	[NSApp endSheet:commitMessageWindow];
}

- (void)reloadTasks
{
	[taskController rearrangeObjects];
	[taskTable reloadData];
//	[taskTable setNeedsDisplay:YES];
//	[[NSRunLoop currentRunLoop] runUntilDate:[NSDate dateWithTimeIntervalSinceNow:0.01]];
//	NSLog(@"full task reload");
}

- (void)setProgressVisible:(NSNumber *)visible
{
	if ([visible boolValue]) {
		if ([[NSUserDefaults standardUserDefaults] boolForKey:@"showProgressPanel"]) {
			[progressWindow setFrameOrigin:NSMakePoint(10, 10)];
			[progressWindow orderFront:self];	
		}			
	}
	else {
		[self performSelector:@selector(_closeProgressVisible) withObject:nil afterDelay:0.5];
	}
}

- (void)_closeProgressVisible
{
	[progressWindow orderOut:self];
	[progressController removeObjects:[progressController arrangedObjects]];	
}

- (void)addProgressPath:(NSString *)path withAction:(NSString *)action
{
	if (![[NSUserDefaults standardUserDefaults] boolForKey:@"showProgressPanel"])
		return;
	
	NSString *prefix = nil;
	if ([[serverController selectedObjects] count]) {
		id server = [[serverController selectedObjects] objectAtIndex:0];
		prefix = [server valueForKey:@"localPath"];
	}
	if (prefix && [path hasPrefix:prefix])
		path = [path substringFromIndex:[prefix length]];
	
	[progressController performSelectorOnMainThread:@selector(addObject:) withObject:[NSDictionary dictionaryWithObjectsAndKeys:path, @"path", action, @"action", nil] waitUntilDone:YES];
//	[progressController addObject:[NSDictionary dictionaryWithObjectsAndKeys:path, @"path", action, @"action", nil]];
}

@end
