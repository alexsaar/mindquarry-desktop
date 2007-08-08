//
//  MQTask.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 07.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTask.h"
#import "MQTeam.h"

#import "MQUpdateRequest.h"

#import "GrowlNotifications.h"

static BOOL global_autosave_enabled = NO;

static NSTimer *saveTimer = nil;
static NSLock *saveTimerLock = nil;

static int saveTaskCount = 0;

@implementation MQTask

+ (void)initialize
{
    saveTimerLock = [[NSLock alloc] init];
    
	[self setAutoSaveEnabled:NO];
	
	[self setKeys:[NSArray arrayWithObject:@"status"] triggerChangeNotificationsForDependentKey:@"statusIndex"];
	[self setKeys:[NSArray arrayWithObject:@"priority"] triggerChangeNotificationsForDependentKey:@"priorityIndex"];
	[self setKeys:[NSArray arrayWithObjects:@"status", @"statusIndex", @"priority", @"priorityIndex", @"title", @"summary", @"date", @"needsUpdate", nil] triggerChangeNotificationsForDependentKey:@"self"];
	
	[self setKeys:[NSArray arrayWithObject:@"date"] triggerChangeNotificationsForDependentKey:@"sortDate"];
	[self setKeys:[NSArray arrayWithObject:@"date"] triggerChangeNotificationsForDependentKey:@"inspectorDate"];
	
//	[self setKeys:[NSArray arrayWithObjects:@"status", @"statusIndex", @"priority", @"priorityIndex", @"title", @"summary", nil] triggerChangeNotificationsForDependentKey:@"importantData"];
}

+ (void)setAutoSaveEnabled:(BOOL)enabled
{
	global_autosave_enabled = enabled;
//	NSLog(@"autosave %d", enabled);
}

+ (void)saveUnsavedTasks
{
    NSManagedObjectContext *context = [[NSApp delegate] managedObjectContext];
    NSFetchRequest *req = [[[NSFetchRequest alloc] init] autorelease];
    [req setEntity:[NSEntityDescription entityForName:@"Task" inManagedObjectContext:context]];
    [req setPredicate:[NSPredicate predicateWithFormat:@"needsUpdate = YES"]];
    NSArray *unsavedTasks = [context executeFetchRequest:req error:nil];
    NSEnumerator *taskEnum = [unsavedTasks objectEnumerator];
    id task;
    saveTaskCount = 0;
    while (task = [taskEnum nextObject]) {
//        NSLog(@"saving unsaved %@", [task valueForKey:@"title"]);
        [task save];
        saveTaskCount++;
    }
//    NSLog(@"re-tried to commit %d unsaved tasks", saveTaskCount);
    
    if (saveTaskCount)
        [[[NSApp delegate] valueForKey:@"controller"] setValue:[NSNumber numberWithBool:YES] forKey:@"hasUnsavedTasks"];
}

- (void)setAutoSaveEnabled:(BOOL)enabled
{
	autosave_enabled = enabled;
}

- (id)init
{
	if (![super init])
		return self;
	
	autosave_enabled = YES;
	
	return self;
}

- (void)dealloc
{
	[saveTimer invalidate];
	[saveTimer release];
	saveTimer = nil;
	
	[super dealloc];
}

- (int)statusIndex
{
	NSString *status = [self valueForKey:@"status"];
	if (!status)
		return 0;
	
	if ([status isEqualToString:@"new"])
		return 0;	
	if ([status isEqualToString:@"running"])
		return 1;
	if ([status isEqualToString:@"paused"])
		return 2;
	if ([status isEqualToString:@"done"])
		return 3;
	
	return 0;
}

- (void)setStatusIndex:(int)_index
{
	NSString *status = nil;
	
	switch (_index) {
	case 0:
		status = @"new";
		break;
		
	case 1:
		status = @"running";
		break;
		
	case 2:
		status = @"paused";
		break;
		
	case 3:
		status = @"done";
		break;
		
	default:
		break;
	}
	
	[self setValue:status forKey:@"status"];
}

- (int)priorityIndex
{
	NSString *prio = [self valueForKey:@"priority"];
	if (!prio)
		return 1;
	
	if ([prio isEqualToString:@"low"])
		return 0;	
	if ([prio isEqualToString:@"medium"])
		return 1;
	if ([prio isEqualToString:@"important"])
		return 2;
	if ([prio isEqualToString:@"critical"])
		return 3;
	
	return 1;
}

- (void)setPriorityIndex:(int)_index
{
	NSString *prio = nil;
	
	switch (_index) {
		case 0:
			prio = @"low";
			break;
			
		case 1:
			prio = @"medium";
			break;
			
		case 2:
			prio = @"important";
			break;
			
		case 3:
			prio = @"critical";
			break;
			
		default:
			break;
	}
	
	[self setValue:prio forKey:@"priority"];
}

- (void)save
{
	if ([self valueForKey:@"title"] == nil)
		return;
    
    if (isSaving)
        return;
    
    isSaving = YES;
	
//	NSLog(@"saving task %@ \"%@\"", [self valueForKey:@"id"], [self valueForKey:@"title"]);
	MQUpdateRequest *request = [[MQUpdateRequest alloc] initWithServer:[[self valueForKey:@"team"] valueForKey:@"server"] forTask:self];
	[request performSelectorOnMainThread:@selector(addToQueue) withObject:nil waitUntilDone:YES];
	[request autorelease];
}

- (void)finishSave
{
    [self setValue:[NSNumber numberWithBool:YES] forKey:@"existsOnServer"];
    [self setValue:[NSNumber numberWithBool:NO] forKey:@"needsUpdate"];
    isSaving = NO;
    
    [saveTimerLock lock];
    saveTaskCount--;
    if (saveTaskCount == 0) 
        [[[NSApp delegate] valueForKey:@"controller"] setValue:[NSNumber numberWithBool:NO] forKey:@"hasUnsavedTasks"];
    [saveTimerLock unlock];
	
	// send a growl notification
	[GrowlApplicationBridge notifyWithTitle:@"Task Saved"
								description:[self valueForKey:@"title"]
						   notificationName:GROWL_TASK_SAVED
								   iconData:nil
								   priority:0
								   isSticky:NO
							   clickContext:[[self objectID] URIRepresentation]];
}

- (NSString *)dueDescription
{
	NSDate *due = [self valueForKey:@"date"];
	if (!due)
		return nil;
	
	NSDate *now = [NSDate date];
	
	NSTimeInterval delta = [due timeIntervalSinceDate:now];
	
	int days = ceil(delta / 60 / 60 / 24);
	
	if (days == 0)
		return @"today";
	else if (days == 1)
		return @"tomorrow";
	else if (days == -1)
		return @"yesterday";
	
	NSString *timeString = nil;
//	if (days < 10)
		timeString = [NSString stringWithFormat:@"%d day%@", ABS(days), ABS(days) > 1 ? @"s" : @""];
//	else
		
	if (delta > 0)
		return [NSString stringWithFormat:@"%@ left", timeString];
	else
		return [NSString stringWithFormat:@"%@ ago", timeString];
}

//- (id)importantData
//{
//	return nil;
//}
//
//- (void)setImportantData:(id)data
//{
//	[self save];
//}
//
- (void)didChangeValueForKey:(NSString *)key
{
	[super didChangeValueForKey:key];
	if (!global_autosave_enabled || !autosave_enabled)
		return;

	if ([key isEqualToString:@"server"] || 
		[key isEqualToString:@"team"] || 
		[key isEqualToString:@"upToDate"] || 
		[key isEqualToString:@"id"] || 
		[key isEqualToString:@"existsOnServer"] || 
        [key isEqualToString:@"needsUpdate"])
		return;
	
    [self setValue:[NSNumber numberWithBool:YES] forKey:@"needsUpdate"];
    
    [saveTimerLock lock];
    
	if (saveTimer) {
		[saveTimer invalidate];
		[saveTimer release];
	}
	
	saveTimer = [[NSTimer scheduledTimerWithTimeInterval:1 target:[self class] selector:@selector(saveUnsavedTasks) userInfo:nil repeats:NO] retain];
    
    [saveTimerLock unlock];
	
//	NSLog(@"%@ change val for key %@", [self valueForKey:@"title"], key);
}

- (NSDate *)sortDate
{
	NSDate *d = [self valueForKey:@"date"];
	if (!d)
		return [NSDate distantFuture];
	return d;
}

- (NSDate *)inspectorDate
{
	NSDate *d = [self valueForKey:@"date"];
	if (!d)
		return [NSDate date];
	return d;
}

- (void)setInspectorDate:(NSDate *)date
{
	[self setValue:date forKey:@"date"];
}

- (NSURL *)webURL
{
	return [NSURL URLWithString:[self valueForKey:@"id"] relativeToURL: [[self valueForKey:@"team"] tasksURL]];
}

@end
