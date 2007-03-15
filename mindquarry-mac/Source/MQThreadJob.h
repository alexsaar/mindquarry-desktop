//
//  MQThreadJob.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 15.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQJob.h"

@interface MQThreadJob : MQJob {

}

- (void)threadMethod;

- (void)_threadHelper;

@end
