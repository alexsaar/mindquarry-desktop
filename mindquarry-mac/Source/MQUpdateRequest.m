//
//  MQUpdateRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 07.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQUpdateRequest.h"


@implementation MQUpdateRequest

- (NSData *)putData
{
	NSXMLElement *root = [[NSXMLElement alloc] initWithName:@"task"];
	[root setAttributesAsDictionary:[NSDictionary dictionaryWithObjectsAndKeys:[url absoluteString], @"xml:base", nil]];
	
	NSXMLElement *element = nil;
	
	if ([task valueForKey:@"title"] && [[task valueForKey:@"title"] length] > 0) {
		element = [[[NSXMLElement alloc] initWithName:@"title"] autorelease];
		[element setStringValue:[task valueForKey:@"title"]];
		[root addChild:element];
	}
	
	if ([task valueForKey:@"priority"] && [[task valueForKey:@"priority"] length] > 0) {
		element = [[[NSXMLElement alloc] initWithName:@"priority"] autorelease];
		[element setStringValue:[task valueForKey:@"priority"]];
		[root addChild:element];
	}
	
	if ([task valueForKey:@"summary"] && [[task valueForKey:@"summary"] length] > 0) {
		element = [[[NSXMLElement alloc] initWithName:@"summary"] autorelease];
		[element setStringValue:[task valueForKey:@"summary"]];
		[root addChild:element];
	}

	if ([task valueForKey:@"status"] && [[task valueForKey:@"status"] length] > 0) {
		element = [[[NSXMLElement alloc] initWithName:@"status"] autorelease];
		[element setStringValue:[task valueForKey:@"status"]];
		[root addChild:element];
	}
	
	if ([task valueForKey:@"descHTML"] && [[task valueForKey:@"descHTML"] length] > 0) {
		element = [[[NSXMLElement alloc] initWithName:@"description"] autorelease];
		[element setStringValue:[task valueForKey:@"descHTML"]];
		[root addChild:element];
	}
	
	if ([task valueForKey:@"date"]) {
		NSDateFormatter *formatter = [[NSDateFormatter alloc] initWithDateFormat:@"%m/%d/%Y" allowNaturalLanguage:NO];
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

- (void)handleResponseData:(NSData *)data
{

}

@end
