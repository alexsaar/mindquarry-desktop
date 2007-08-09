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
	
	id selectedTeam;
    
    NSPredicate *predicate;
	
}

+ (void)cancelCurrentJob;

- (id)initWithServer:(id)_server selectedTeam:(id)_selectedTeam predicate:(NSPredicate *)_pred synchronizes:(BOOL)_synchronizes;

@end
