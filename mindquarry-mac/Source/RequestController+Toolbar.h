//
//  RequestController+Toolbar.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 19.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "RequestController.h"

#define TASKS_TOOLBAR_ID @"MQDesktopMainToolbar2"
#define FILES_TOOLBAR_ID @"MQDesktopWorkToolbar2"

@interface RequestController (Toolbar) 

- (void)initToolbars;

- (IBAction)selectMode:(id)sender;

@end
