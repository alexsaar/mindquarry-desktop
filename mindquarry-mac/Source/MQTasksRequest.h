//
//  MQTasksRequest.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQRequest.h"

@interface MQTasksRequest : MQRequest {

	id team;
	
}

- (id)initWithServer:(id)_server forTeam:(id)_team;

@end
