//
//  MQSVNCommitJob.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 16.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQThreadJob.h"

@interface MQSVNCommitJob : MQThreadJob {

	BOOL cancel;
	
	id currentTeam;
	
}

@end
