//
//  MQTeamsRequest.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTeamsRequest.h"

#import "RequestController.h"
#import "MQTasksRequest.h"

@implementation MQTeamsRequest

- (NSURL *)url
{
	return [self currentURLForPath:@"teams"];
}

- (void)parseXMLResponse:(NSXMLDocument *)document
{
	NSXMLElement *root = [document rootElement];

	int i;
	int count = [root childCount];
	for (i = 0; i < count; i++) {
		id team = [root childAtIndex:i];
		if (![[team name] isEqualToString:@"teamspace"])
			continue;
		if (![team isKindOfClass:[NSXMLElement class]])
			continue;

		NSString *name = [team stringValue];		
		NSString *team_id = [[team attributeForName:@"xlink:href"] stringValue];
		
		if (!name || !team_id)
			continue;
		
//		NSLog(@"team %@ name %@", team_id, name);

		id teamobj = [controller teamWithId:team_id forServer:server];
		
		[teamobj setValue:name forKey:@"name"];
				
		MQTasksRequest *treq = [[MQTasksRequest alloc] initWithController:controller forServer:server forTeam:teamobj];
		[treq addToQueue];
		[treq autorelease];
	}
}

- (void)finishRequest
{
	
}

@end
