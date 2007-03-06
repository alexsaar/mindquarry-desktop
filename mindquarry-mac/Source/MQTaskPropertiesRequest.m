//
//  MQTaskPropertiesRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTaskPropertiesRequest.h"


@implementation MQTaskPropertiesRequest

- (id)initWithController:(RequestController *)_controller forServer:(id)_server forTask:(id)_task
{
	if (![super initWithController:_controller forServer:_server])
		return nil;
	
	task = [_task retain];
	
	return self;
}

- (void)dealloc
{
	[task release];
	task = nil;
	
	[super dealloc];
}

- (NSURL *)url
{
	NSString *taskID = [task valueForKey:@"id"];
	NSString *teamID = [[task valueForKey:@"team"] valueForKey:@"id"];
	if (!taskID || !teamID)
		return nil;
	return [self currentURLForPath:[NSString stringWithFormat:@"tasks/%@/%@", teamID, taskID]];
}

- (void)parseXMLResponse:(NSXMLDocument *)document
{
	NSXMLElement *root = [document rootElement];

	int i;
	int count = [root childCount];
	for (i = 0; i < count; i++) {
		id node = [root childAtIndex:i];
		
		if ([[node name] isEqualToString:@"title"]) {
			[task setValue:[node stringValue] forKey:@"title"];
		}
		else if ([[node name] isEqualToString:@"status"]) {
			[task setValue:[node stringValue] forKey:@"status"];
		}
		else if ([[node name] isEqualToString:@"priority"]) {
			[task setValue:[node stringValue] forKey:@"priority"];
		}
		else if ([[node name] isEqualToString:@"summary"]) {
			[task setValue:[node stringValue] forKey:@"summary"];
		}
		else if ([[node name] isEqualToString:@"description"]) {
			NSString *descVal = [node stringValue];
			[self performSelector:@selector(setDescription:) withObject:descVal afterDelay:0.1];
		}	
		else if ([[node name] isEqualToString:@"date"]) {
			NSString *dateVal = [node stringValue];
			NSDate *date = [NSDate dateWithNaturalLanguageString:dateVal];
			if (date)
				[task setValue:date forKey:@"date"];
		}
	}
}

- (void)setDescription:(NSString *)desc
{
	NSData *data = [desc dataUsingEncoding:NSUTF8StringEncoding];
	NSAttributedString *string = [[NSAttributedString alloc] initWithHTML:data documentAttributes:nil];
	NSData *sdata = [string RTFFromRange:NSMakeRange(0, [string length]) documentAttributes:nil];
	[task setValue:sdata forKey:@"desc"];
	[string release];
}

@end
