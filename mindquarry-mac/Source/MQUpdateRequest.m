//
//  MQUpdateRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 07.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQUpdateRequest.h"
#import "MQTask.h"

@implementation MQUpdateRequest

- (void)startRequest
{
	if (![task valueForKey:@"title"]) {
		NSLog(@"Warning: task %@ has no title", [self url]);
		return;
	}
	[super startRequest];
}

- (NSData *)putData
{
	NSXMLElement *root = [[NSXMLElement alloc] initWithName:@"task"];
	[root setAttributesAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:[url absoluteString], @"xml:base", nil]];
	
	NSXMLElement *element = nil;
	
	if ([task valueForKey:@"title"]) {
		element = [[[NSXMLElement alloc] initWithName:@"title"] autorelease];
		[element setStringValue:[task valueForKey:@"title"]];
		[root addChild:element];
	}
	
	if ([task valueForKey:@"priority"]) {
		element = [[[NSXMLElement alloc] initWithName:@"priority"] autorelease];
		[element setStringValue:[task valueForKey:@"priority"]];
		[root addChild:element];
	}
	
	if ([task valueForKey:@"summary"]) {
		element = [[[NSXMLElement alloc] initWithName:@"summary"] autorelease];
		[element setStringValue:[task valueForKey:@"summary"]];
		[root addChild:element];
	}

	if ([task valueForKey:@"status"]) {
		element = [[[NSXMLElement alloc] initWithName:@"status"] autorelease];
		[element setStringValue:[task valueForKey:@"status"]];
		[root addChild:element];
	}
	
	if ([task valueForKey:@"descHTML"]) {
		element = [[[NSXMLElement alloc] initWithName:@"description"] autorelease];
		[element setStringValue:[task valueForKey:@"descHTML"]];
		[root addChild:element];
	}
	
	if ([task valueForKey:@"date"]) {
		NSDateFormatter *formatter = [[NSDateFormatter alloc] initWithDateFormat:@"%Y-%m-%d" allowNaturalLanguage:NO];
		[formatter setFormatterBehavior:NSDateFormatterBehavior10_0];
		NSString *dateString = [formatter stringFromDate:[task valueForKey:@"date"]];
		if (dateString) {
			element = [[[NSXMLElement alloc] initWithName:@"date"] autorelease];
			[element setStringValue:dateString];
			[root addChild:element];
		}
		else
			NSLog(@"Warning: failed to generate string from date %@", [task valueForKey:@"date"]);
		[formatter release];
	}
	
	NSXMLDocument *document = [[NSXMLDocument alloc] initWithRootElement:root];
	[root release];
	
	NSData *data = [document XMLData];
	
//	NSLog(@"xml data %@", [[[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding] autorelease]);
	
	[document release];
	
	return data;
}

- (NSURL *)url
{
	if ([[task valueForKey:@"existsOnServer"] boolValue])
		return [super url];
	NSString *teamID = [[task valueForKey:@"team"] valueForKey:@"id"];
	if (!teamID)
		return nil;
	return [self currentURLForPath:[NSString stringWithFormat:@"tasks/%@/new", teamID]];
}

- (NSURLRequest *)request
{
	return [self putRequestForURL:url withData:[self putData]];
}

- (NSURLRequest *)putRequestForURL:(NSURL *)_url withData:(NSData *)_data
{
	NSMutableURLRequest *_request = [[NSMutableURLRequest alloc] init];
	[_request setURL:_url];
	[_request setHTTPMethod:@"PUT"];
	[_request setHTTPBody:_data];
	[_request setValue:@"text/xml" forHTTPHeaderField:@"accept"];
	return [_request autorelease];
}

- (NSURLRequest *)connection:(NSURLConnection *)connection willSendRequest:(NSURLRequest *)request redirectResponse:(NSURLResponse *)redirectResponse
{
	if (![[task valueForKey:@"existsOnServer"] boolValue]) {
		NSString *taskID = [[[request URL] absoluteString] lastPathComponent];
		[task setValue:taskID forKey:@"id"];
	}
	return request;
}

- (void)handleResponseData:(NSData *)data
{
    [task performSelectorOnMainThread:@selector(finishSave:) withObject:[NSNumber numberWithBool:YES] waitUntilDone:NO];
}

- (NSString *)statusString
{
	return @"Saving changes...";
}

- (void)handleHTTPErrorCode:(int)statusCode
{
	NSLog(@"HTTP Error %d has been encountered while trying to save the task %@", statusCode, [task valueForKey:@"title"]);
	
	// NSRunAlertPanel(@"Failed to save task", [NSString stringWithFormat:@"HTTP Error %d has been encountered while trying to save the task %@", statusCode, [task valueForKey:@"title"]], @"OK", nil, nil);
	
    [task performSelectorOnMainThread:@selector(finishSave:) withObject:[NSNumber numberWithBool:NO] waitUntilDone:NO];
}

@end
