//
//  MQJob.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 15.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#define MAX_CONNECTION 1


@interface MQJob : NSObject {

	@protected
	
	id server;
	BOOL didFree;
	
}

+ (void)increaseRequestCount:(id)sender;
+ (void)decreaseRequestCount:(id)sender;

- (id)initWithServer:(id)_server;

- (void)addToQueue;
- (void)startRequest;
- (void)finishRequest;
- (void)cancel;

- (NSString *)statusString;

@end
