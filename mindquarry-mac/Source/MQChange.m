//
//  MQChange.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 19.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQChange.h"


@implementation MQChange

+ (void)initialize
{
	[self setKeys:[NSArray arrayWithObjects:@"enabled", nil] triggerChangeNotificationsForDependentKey:@"self"];
}

- (void)revealInFinder
{
	NSAppleScript *revealScript = [[NSAppleScript alloc] initWithSource:[NSString stringWithFormat:@"tell application \"Finder\" to reveal posix file \"%@\"", [self valueForKey:@"absPath"]]];
	[revealScript executeAndReturnError:nil];
	[revealScript release];
}

- (long)fileSize
{
	if (fileSize == 0) {
		BOOL isDir;
		if ([[NSFileManager defaultManager] fileExistsAtPath:[self valueForKey:@"absPath"] isDirectory:&isDir]) {
			if (isDir)
				fileSize = -1;
			else {
				fileSize = [[[[NSFileManager defaultManager] fileAttributesAtPath:[self valueForKey:@"absPath"] traverseLink:YES] valueForKey:NSFileSize] longValue];
				if (fileSize == 0)
					fileSize = -1;				
			}
		}
	}
	
	if (fileSize == -1)
		return 0;
	
	return fileSize;
}

- (NSString *)fileKind
{
	if (!kind) {
		CFStringRef newKind;
		NSURL *url = [NSURL fileURLWithPath:[self valueForKey:@"absPath"]];
		if (LSCopyKindStringForURL((CFURLRef)url, &newKind) == noErr) 
			kind = [(NSString *)newKind copy];
	}
	return kind;
}

@end
