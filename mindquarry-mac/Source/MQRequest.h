//
//  MQRequest.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@class RequestController;

#define MAX_CONNECTION 2

@interface MQRequest : NSObject {

	@protected
	RequestController *controller;
	NSURL *url;
	
	id server;
	
	NSString *username;
	NSString *password;
	
	@private
	NSURLConnection *_connection;
	NSMutableData *responseData;
	
	BOOL didFree;
	
}

+ (void)increaseRequestCount:(id)sender;

+ (void)decreaseRequestCount;

- (id)initWithController:(RequestController *)_controller forServer:(id)_server;

- (void)addToQueue;

- (void)startRequest;

- (void)finishRequest;

- (void)cancel;

- (NSURL *)url;

- (NSURL *)currentBaseURL;

- (NSURL *)currentURLForPath:(NSString *)path;

- (NSString *)statusString;

- (void)handleResponseData:(NSData *)data;

- (void)parseXMLResponse:(NSXMLDocument *)document;

- (NSURLRequest *)request;

- (NSURLRequest *)xmlRequestForURL:(NSURL *)_url;

@end
