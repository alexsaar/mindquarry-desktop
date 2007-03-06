//
//  MQTasksRequest.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQRequest.h"

@class RequestController;

@interface MQTasksRequest : MQRequest {

	id team;
	
}

- (id)initWithController:(RequestController *)_controller forServer:(id)_server forTeam:(id)_team;

@end
