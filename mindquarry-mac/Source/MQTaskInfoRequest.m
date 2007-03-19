//
//  MQTaskInfoRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 14.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTaskInfoRequest.h"

#import "MQTeam.h"

@implementation MQTaskInfoRequest

- (NSURL *)url
{
	NSString  *teamID = [team valueForKey:@"id"];
	if (!teamID)
		return nil;
	return [self currentURLForPath:[NSString stringWithFormat:@"/teams/team/%@/", teamID]];
}

- (void)parseXMLResponse:(NSXMLDocument *)document
{
	NSXMLElement *root = [document rootElement];
	
	int count = [root childCount];
	int i;
	for (i = 0; i < count; i++) {
		NSXMLNode *node = [root childAtIndex:i];
		if ([[node name] isEqualToString:@"workspace"])			
			[team setValue:[node stringValue] forKey:@"workspaceURL"];
	}
	
//	NSLog(@"workspace: %@ -> %@", [team valueForKey:@"workspaceURL"], [team localPath]);
}

@end
