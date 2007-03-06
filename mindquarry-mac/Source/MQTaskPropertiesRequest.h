//
//  MQTaskPropertiesRequest.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQRequest.h"

@class RequestController;

@interface MQTaskPropertiesRequest : MQRequest {

	id task;
	
}

- (id)initWithController:(RequestController *)_controller forServer:(id)_server forTask:(id)_task;

- (void)setDescription:(NSString *)desc;

@end
