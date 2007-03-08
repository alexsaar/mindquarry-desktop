//
//  MQRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQRequest.h"

#import "MQServer.h"

static NSLock *spinner_lock = nil;
static int request_running_count = 0;

@implementation MQRequest

+ (void)initialize
{
	spinner_lock = [[NSLock alloc] init];
}

+ (void)increaseRequestCount:(id)sender
{
	[spinner_lock lock];
	
	request_running_count++;
	
	id spinner = [[NSApp delegate] valueForKey:@"statusSpinner"];
	[spinner setHidden:NO];
	[spinner startAnimation:self];
	
	NSString *message = nil;
	
	if ([sender respondsToSelector:@selector(statusString)])
		message = [sender statusString];
	else {
//		NSLog(@"sender %@ has no msg", sender);
		message = @"request...";		
	}
	
	id field = [[NSApp delegate] valueForKey:@"statusField"];
	[field setStringValue:message];
	[field setHidden:NO];
	
	[spinner_lock unlock];
}

+ (void)decreaseRequestCount
{
	[spinner_lock lock];
	
	request_running_count--;

	if (request_running_count == 0) {
		id spinner = [[NSApp delegate] valueForKey:@"statusSpinner"];
		[spinner stopAnimation:self];
		[spinner setHidden:YES];
		
		id field = [[NSApp delegate] valueForKey:@"statusField"];
		[field setHidden:YES];
	}
	
	[spinner_lock unlock];
}

- (id)initWithController:(RequestController *)_controller forServer:(id)_server
{
	if (![super init])
		return nil;
	
	controller = [_controller retain];
	server = [_server retain];
	
	[self setValue:[server valueForKey:@"username"] forKey:@"username"];
	[self setValue:[server valueForKey:@"password"] forKey:@"password"];
		
	return self;
}

- (void)dealloc
{
	[connection cancel];
	[connection release];
	connection = nil;
	
	[responseData release];
	responseData = nil;
	
	[username release];
	username = nil;
	
	[password release];
	password = nil;
	
	[url release];
	url = nil;
	
	[controller release];
	controller = nil;

	[server release];
	server = nil;
	
	[super dealloc];
}

- (void)addToQueue
{
	[[self valueForKey:@"server"] enqueueRequest:self];
}

- (void)startRequest
{
	
//	[self retain];

	[self setValue:[self url] forKey:@"url"];
//	NSLog(@"url %@", [[self valueForKey:@"url"] absoluteString]);
	
	if (![self valueForKey:@"url"]) {
		[self finishRequest];
		return;
	}
	
//	NSLog(@"starting request %@", [[self valueForKey:@"url"] absoluteString]);
	
	[responseData release];
	responseData = [[NSMutableData alloc] init];
	
	connection = [[NSURLConnection connectionWithRequest:[self request] delegate:self] retain];
//	if (!connection)
//		[self autorelease];
}

- (void)handleResponseData:(NSData *)data
{
	NSError *error;
	NSXMLDocument *document = [[NSXMLDocument alloc] initWithData:data options:0 error:&error]; 

	[self parseXMLResponse:document];
	
	[document release];
}

- (void)parseXMLResponse:(NSXMLDocument *)document
{
	
}

- (void)finishRequest
{
	[[self valueForKey:@"server"] runFromQueueIfNeeded];
	[[self class] decreaseRequestCount];
}

- (NSURL *)url
{
	return nil;
}

- (NSURL *)currentBaseURL
{
	NSString *baseURL = [server valueForKey:@"baseURL"];
	if (!baseURL || [baseURL length] == 0)
		return nil;
	return [NSURL URLWithString:baseURL];
}

- (NSURL *)currentURLForPath:(NSString *)path
{
	if (!path || [path length] == 0)
		return nil;
	NSURL *base = [self currentBaseURL];
	if (!base)
		return nil;
	return [NSURL URLWithString:path relativeToURL:base];
}

- (NSString *)statusString
{
	return @"Updating tasks...";
}

- (NSURLRequest *)request
{
	return [self xmlRequestForURL:url];
}

- (NSURLRequest *)xmlRequestForURL:(NSURL *)_url
{
	NSMutableURLRequest *_request = [[NSMutableURLRequest alloc] init];
	[_request setURL:_url];
	[_request setValue:@"text/xml" forHTTPHeaderField:@"accept"];
	return [_request autorelease];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
	[responseData appendData:data];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
	NSLog(@"connection failed loading error %@", error);
	
	[responseData release];
	responseData = nil;
	
//	[self autorelease];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{	
//	NSLog(@"connection finished loading %@", [url absoluteString]);
	
	[self handleResponseData:responseData];
	
	[responseData release];
	responseData = nil;
	
	[self finishRequest];
	
	// TODO
//	[self autorelease];
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
	NSURLCredential *cred = [NSURLCredential credentialWithUser:username password:password persistence:NSURLCredentialPersistenceForSession];
	[[challenge sender] useCredential:cred forAuthenticationChallenge:challenge];
//	NSLog(@"credentials sent %@:%@", username, password);
}

- (NSURLRequest *)connection:(NSURLConnection *)connection willSendRequest:(NSURLRequest *)request redirectResponse:(NSURLResponse *)redirectResponse
{
	return request;
}

@end
