//
//  MQTask.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 07.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface MQTask : NSManagedObject {
	
	BOOL autosave_enabled;
    
    BOOL isSaving;
	
}

+ (void)setAutoSaveEnabled:(BOOL)enabled;

+ (void)saveUnsavedTasks;

- (void)setAutoSaveEnabled:(BOOL)enabled;

- (int)statusIndex;

- (void)setStatusIndex:(int)_index;

- (int)priorityIndex;

- (void)setPriorityIndex:(int)_index;

- (void)save;
- (void)finishSave;

- (NSString *)dueDescription;

- (NSDate *)sortDate;

- (NSDate *)inspectorDate;

- (void)setInspectorDate:(NSDate *)date;

- (NSURL *)webURL;

//- (id)importantData;
//
//- (void)setImportantData:(id)data;

@end
