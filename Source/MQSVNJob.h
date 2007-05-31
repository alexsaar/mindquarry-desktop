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
    
    NSPredicate *predicate;
	
}

+ (void)cancelCurrentJob;

- (id)initWithServer:(id)_server predicate:(NSPredicate *)_pred synchronizes:(BOOL)_synchronizes;

@end
