//
//  MQChange.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 19.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface MQChange : NSManagedObject {

	long fileSize;
	
}

- (void)revealInFinder;

- (long)fileSize;

- (NSString *)fileKind;

@end
