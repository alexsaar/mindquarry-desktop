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
	
	BOOL wasZero = request_running_count == 0;
//	NSLog(@"++ request_running_count %d" ,request_running_count);
	
	request_running_count++;
	
	if (wasZero) {
		
//		NSLog(@" === start busy");
		
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
		
		id tbItem = [[NSApp delegate] valueForKey:@"refreshToolbarItem"];		
		[tbItem setEnabled:NO];
//		[tbItem setImage:[NSImage imageNamed:@"AlertStopIcon"]];
//		[tbItem setLabel:@"Stop"];
		
		id stopItem = [[NSApp delegate] valueForKey:@"stopToolbarItem"];
		[stopItem setEnabled:YES];
	}
	
	[spinner_lock unlock];
}

+ (void)decreaseRequestCount
{
	[spinner_lock lock];
	
//	NSLog(@"-- request_running_count %d" ,request_running_count);
	
	request_running_count--;
	
	if (request_running_count == 0) {
		
//		NSLog(@" === stop busy");
		
		id spinner = [[NSApp delegate] valueForKey:@"statusSpinner"];
		[spinner stopAnimation:self];
		[spinner setHidden:YES];
		
		id field = [[NSApp delegate] valueForKey:@"statusField"];
		[field setHidden:YES];
		
		id tbItem = [[NSApp delegate] valueForKey:@"refreshToolbarItem"];		
		[tbItem setEnabled:YES];

		id stopItem = [[NSApp delegate] valueForKey:@"stopToolbarItem"];
		[stopItem setEnabled:NO];
	}
	
	[spinner_lock unlock];
}

- (id)initWithController:(RequestController *)_controller forServer:(id)_server
{
	if (![super init])
		return nil;
	
	didFree = YES;
	
	controller = [_controller retain];
	server = [_server retain];
	
	[self setValue:[server valueForKey:@"username"] forKey:@"username"];
	[self setValue:[server valueForKey:@"password"] forKey:@"password"];
		
	return self;
}

- (void)dealloc
{
	[_connection cancel];
	[_connection autorelease];
	_connection = nil;
	
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
	[self setValue:[self url] forKey:@"url"];
//	NSLog(@"url %@", [[self valueForKey:@"url"] absoluteString]);
	
	if (![self valueForKey:@"url"]) {
		[self finishRequest];
		return;
	}
	
//	NSLog(@"%@ retcount %d", self, [self retainCount]);
	
	didFree = NO;
	[self retain];
	
//	NSLog(@"starting request %@", [[self valueForKey:@"url"] absoluteString]);
	
	[responseData release];
	responseData = [[NSMutableData alloc] init];
	
	[_connection autorelease];
	_connection = nil;
	_connection = [[NSURLConnection connectionWithRequest:[self request] delegate:self] retain];
	if (!_connection) {
		[self finishRequest];
	}
}

- (void)handleResponseData:(NSData *)data
{
//	NSLog(@"data %@", [[[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding] autorelease]);
	
	NSError *error;
	NSXMLDocument *document = [[NSXMLDocument alloc] initWithData:data options:0 error:&error]; 

//	NSLog(@"%@", document);
	
	[self parseXMLResponse:document];
	
	[document release];
}

- (void)parseXMLResponse:(NSXMLDocument *)document
{
	
}

- (void)finishRequest
{
	if (!didFree) {
		[[self valueForKey:@"server"] runFromQueueIfNeeded:self];
		[[self class] decreaseRequestCount];
		
		[self autorelease];
//		NSLog(@"release");
		didFree = YES;
	}
}

- (void)cancel
{
	[_connection cancel];
	[_connection autorelease];
	_connection = nil;
	
//	[self finishRequest];
	if (!didFree) {
		[self autorelease];
		didFree = YES;
	}
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
	NSLog(@"URL request failed: %@", error);
	
	[responseData release];
	responseData = nil;
	
	[self finishRequest];
	
	[_connection autorelease];
	_connection = nil;
	
	[self finishRequest];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{	
//	NSLog(@"connection finished loading %@", [url absoluteString]);
	
	[self handleResponseData:responseData];
	
	[responseData release];
	responseData = nil;
	
	[self finishRequest];
	
	[_connection autorelease];
	_connection = nil;
	
	[self finishRequest];
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

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
	if ([response isKindOfClass:[NSHTTPURLResponse class]]) {
		int status = [(NSHTTPURLResponse *)response statusCode];
		if (status >= 400) {			
			[_connection cancel];
			[_connection autorelease];
			_connection = nil;
			
			NSDictionary *errorInfo = [NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Server returned status code %d", status], NSLocalizedDescriptionKey, [url absoluteString], NSErrorFailingURLStringKey, nil];
			NSError *statusError = [NSError errorWithDomain:NSHTTPPropertyStatusCodeKey code:status userInfo:errorInfo];
			[self connection:connection didFailWithError:statusError];
		}
	}
}

@end
