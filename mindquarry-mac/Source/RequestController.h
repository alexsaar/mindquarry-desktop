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

	
}

- (IBAction)test:(id)sender;

- (id)selectedServer;

- (id)teamWithId:(NSString *)team_id forServer:(id)server;

- (id)taskWithId:(NSString *)task_id forTeam:(id)team;

@end
