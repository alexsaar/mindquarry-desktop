//
//  MQTask.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 07.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface MQTask : NSManagedObject {

}

- (int)statusIndex;

- (void)setStatusIndex:(int)_index;

- (int)priorityIndex;

- (void)setPriorityIndex:(int)_index;

- (void)save;

- (NSString *)dueDescription;

//- (id)importantData;
//
//- (void)setImportantData:(id)data;

@end
