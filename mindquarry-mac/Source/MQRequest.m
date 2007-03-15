//
//  MQRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQRequest.h"

#import "MQServer.h"

@implementation MQRequest

- (id)initWithServer:(id)_server;
{
	if (![super initWithServer:_server])
		return nil;
	
		
	[self setValue:[_server valueForKey:@"username"] forKey:@"username"];
	[self setValue:[_server valueForKey:@"password"] forKey:@"password"];
		
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
	
	[super dealloc];
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
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
	if ([challenge previousFailureCount] > 0) {
		NSLog(@"Authentication failed (username: %@)", username);
		[[challenge sender] cancelAuthenticationChallenge:challenge];
		return;
	}
	
	if (!username || [username length] == 0) {
//		NSLog(@"no username, trying without auth");
		[[challenge sender] continueWithoutCredentialForAuthenticationChallenge:challenge];
		return;
	}
	
	if (!password)
		password = @"";
	
	NSURLCredential *cred = [NSURLCredential credentialWithUser:username password:password persistence:NSURLCredentialPersistenceForSession];
	[[challenge sender] useCredential:cred forAuthenticationChallenge:challenge];
	
	[server setValue:cred forKey:@"credential"];
	[server setValue:[challenge protectionSpace] forKey:@"protectionSpace"];
	
//	NSLog(@"credentials sent <%@:%@>", username, password);
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
			[self handleHTTPErrorCode:status];
		}
	}
}

- (void)handleHTTPErrorCode:(int)statusCode
{
	NSDictionary *errorInfo = [NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Server returned status code %d", statusCode], NSLocalizedDescriptionKey, [url absoluteString], NSErrorFailingURLStringKey, nil];
	NSError *statusError = [NSError errorWithDomain:NSHTTPPropertyStatusCodeKey code:statusCode userInfo:errorInfo];
	[self connection:nil didFailWithError:statusError];
}

@end
