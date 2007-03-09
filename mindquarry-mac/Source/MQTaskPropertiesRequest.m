//
//  MQTaskPropertiesRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTaskPropertiesRequest.h"

#import "MQTask.h"
#import "Mindquarry_Desktop_Client_AppDelegate.h"

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

//	NSLog(@"xml %@", root);
	
	NSString *title = nil;
	NSString *status = nil;
	NSString *priority = nil;
	NSString *summary = nil;
	NSString *description = nil;
	NSString *date = nil;
	
	int i;
	int count = [root childCount];
	for (i = 0; i < count; i++) {
		id node = [root childAtIndex:i];
		
		if ([[node name] isEqualToString:@"title"])			
			title = [node stringValue];

		else if ([[node name] isEqualToString:@"status"])
			status = [node stringValue];
		
		else if ([[node name] isEqualToString:@"priority"])
			priority = [node stringValue];
			
		else if ([[node name] isEqualToString:@"summary"])
			summary = [node stringValue];
		
		else if ([[node name] isEqualToString:@"description"])
			description = [node stringValue];
		
		else if ([[node name] isEqualToString:@"date"])
			date = [node stringValue];
	}
	
	[task setAutoSaveEnabled:NO];
	
	[task setValue:title forKey:@"title"];
	[task setValue:status forKey:@"status"];
	[task setValue:priority forKey:@"priority"];
	[task setValue:summary forKey:@"summary"];

	[task setValue:description forKey:@"descHTML"];

	if (date)
		[task setValue:[NSDate dateWithNaturalLanguageString:date] forKey:@"date"];
	else 
		[task setValue:nil forKey:@"date"];

	[task setValue:server forKey:@"server"];
	
	[task setAutoSaveEnabled:YES];

	[self performSelector:@selector(setDescription:) withObject:description afterDelay:0.1];
}

- (void)setDescription:(NSString *)desc
{	
	[task setAutoSaveEnabled:NO];

	if (!desc)
		[task setValue:nil forKey:@"desc"];

	else {
		NSData *data = [desc dataUsingEncoding:NSUTF8StringEncoding];
		NSAttributedString *string = [[NSAttributedString alloc] initWithHTML:data documentAttributes:nil];
		NSData *sdata = [string RTFFromRange:NSMakeRange(0, [string length]) documentAttributes:nil];
		[task setValue:sdata forKey:@"desc"];
		[string release];		
	}
	
	[task setAutoSaveEnabled:YES];
	
	[[NSApp delegate] reloadTasks];
}

@end
