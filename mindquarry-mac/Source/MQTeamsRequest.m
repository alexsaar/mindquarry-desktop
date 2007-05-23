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
#import "MQTaskInfoRequest.h"

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
        
        // WARNING: abusing xlink:href here to retain compatibility with our
        // internal convention that xlink:href == team ID
        team_id = [[team_id componentsSeparatedByString:@"/"] lastObject];
		
		if (!name || !team_id)
			continue;
		
//		NSLog(@"team %@ name %@", team_id, name);

		id teamobj = [[[NSApp delegate] valueForKey:@"controller"] teamWithId:team_id forServer:server];
		
		[teamobj setValue:name forKey:@"name"];
				
		MQTaskInfoRequest *ireq = [[MQTaskInfoRequest alloc] initWithServer:server forTeam:teamobj];
		[ireq addToQueue];
		[ireq autorelease];
		
		MQTasksRequest *treq = [[MQTasksRequest alloc] initWithServer:server forTeam:teamobj];
		[treq addToQueue];
		[treq autorelease];
	}
}

//- (void)finishRequest
//{
//	
//}

@end
