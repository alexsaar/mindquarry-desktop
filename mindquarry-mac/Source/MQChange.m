//
//  MQChange.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 19.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQChange.h"


@implementation MQChange

- (void)revealInFinder
{
	NSAppleScript *revealScript = [[NSAppleScript alloc] initWithSource:[NSString stringWithFormat:@"tell application \"Finder\" to reveal posix file \"%@\"", [self valueForKey:@"absPath"]]];
	[revealScript executeAndReturnError:nil];
	[revealScript release];
}

@end
