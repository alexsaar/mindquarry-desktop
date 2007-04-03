//
//  MQSVNJob.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 02.04.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQThreadJob.h"

@interface MQSVNJob : MQThreadJob {

	BOOL cancel;
	
	BOOL synchronizes;
	
	id currentTeam;
	
}

- (id)initWithServer:(id)_server synchronizes:(BOOL)_synchronizes;

@end
