//
//  MQRequest.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@class RequestController;

@interface MQRequest : NSObject {

	@protected
	RequestController *controller;
	NSURL *url;
	
	id server;
	
	NSString *username;
	NSString *password;
	
	@private
	NSURLConnection *connection;
	NSMutableData *responseData;
	
}

- (id)initWithController:(RequestController *)_controller forServer:(id)_server;

- (void)startRequest;

- (void)finishRequest;

- (NSURL *)url;

- (NSURL *)currentBaseURL;

- (NSURL *)currentURLForPath:(NSString *)path;

- (void)handleResponseData:(NSData *)data;

- (void)parseXMLResponse:(NSXMLDocument *)document;

- (NSURLRequest *)request;

- (NSURLRequest *)xmlRequestForURL:(NSURL *)_url;

@end
