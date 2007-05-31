//
//  MQUpdateRequest.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 07.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "MQRequest.h"
#import "MQTaskPropertiesRequest.h"

@interface MQUpdateRequest : MQTaskPropertiesRequest {
	
}

- (NSData *)putData;

- (NSURLRequest *)putRequestForURL:(NSURL *)_url withData:(NSData *)_data;

@end
