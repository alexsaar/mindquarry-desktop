//
//  MQRequest.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@class RequestController;

#import "MQJob.h"

@interface MQRequest : MQJob {

	@protected
	NSURL *url;
		
	NSString *username;
	NSString *password;
	
	@private
	NSURLConnection *_connection;
	NSMutableData *responseData;
	
}

- (NSURL *)url;

- (NSURL *)currentBaseURL;

- (NSURL *)currentURLForPath:(NSString *)path;

- (void)handleResponseData:(NSData *)data;

- (void)parseXMLResponse:(NSXMLDocument *)document;

- (void)handleHTTPErrorCode:(int)statusCode;

- (NSURLRequest *)request;

- (NSURLRequest *)xmlRequestForURL:(NSURL *)_url;

@end
