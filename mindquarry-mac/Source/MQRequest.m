//
//  MQRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQRequest.h"


@implementation MQRequest

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

- (void)startRequest
{
	
	[self setValue:[self url] forKey:@"url"];
//	NSLog(@"url: %@", [self url]);
	
	[responseData release];
	responseData = [[NSMutableData alloc] init];
	
	connection = [NSURLConnection connectionWithRequest:[self xmlRequestForURL:url] delegate:self];
	
}

- (void)parseXMLResponse:(NSXMLDocument *)document
{
	
}

- (void)finishRequest
{
	
}

- (NSURL *)url
{
	return nil;
}

- (NSURL *)currentBaseURL
{
	return [NSURL URLWithString:[server valueForKey:@"baseURL"]];
}

- (NSURL *)currentURLForPath:(NSString *)path
{
	return [NSURL URLWithString:path relativeToURL:[self currentBaseURL]];
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
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{	
//	NSLog(@"connection finished loading");
	
	NSError *error;
	NSXMLDocument *document = [[NSXMLDocument alloc] initWithData:responseData options:0 error:&error]; 
	
	[responseData release];
	responseData = nil;
	
	[self parseXMLResponse:document];
	
	[document release];	
	
	[self finishRequest];
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
	NSURLCredential *cred = [NSURLCredential credentialWithUser:username password:password persistence:NSURLCredentialPersistenceForSession];
	[[challenge sender] useCredential:cred forAuthenticationChallenge:challenge];
//	NSLog(@"credentials sent %@:%@", username, password);
}


@end
