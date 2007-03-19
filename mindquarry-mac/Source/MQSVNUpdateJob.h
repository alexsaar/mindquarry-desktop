//
//  MQSVNUpdateJob.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 15.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQThreadJob.h"

@interface MQSVNUpdateJob : MQThreadJob {

	BOOL update;
	
}

- (id)initWithServer:(id)_server updates:(BOOL)_update;

@end
