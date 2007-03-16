//
//  MQTeam.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 13.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@class SVNController;

@interface MQTeam : NSManagedObject {

	SVNController *svn;

	NSArrayController *changesController;
	
}

- (void)initJVM;

- (void)destroyJVM;

- (SVNController *)svnController;

- (NSString *)localPath;

- (void)update;

- (NSArray *)changes;

- (void)commitChanges:(NSArray *)changes message:(NSString *)commitMessage;

- (void)updateLocalPath;

@end
