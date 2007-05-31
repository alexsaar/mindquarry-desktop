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
	[self setKeys:[NSArray arrayWithObjects:@"enabled", @"local", @"onServer", @"absPath", nil] triggerChangeNotificationsForDependentKey:@"self"];
	[self setKeys:[NSArray arrayWithObjects:@"absPath", nil] triggerChangeNotificationsForDependentKey:@"nodeKind"];
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
		if (!kind && LSCopyKindStringForTypeInfo(kLSUnknownType, kLSUnknownCreator, (CFStringRef)[[self valueForKey:@"absPath"] pathExtension], &newKind) == noErr)
			kind = [(NSString *)newKind copy];
	}
	return kind;
}

- (int)nodeKind 
{
	BOOL isDir;
	if ([[NSFileManager defaultManager] fileExistsAtPath:[self valueForKey:@"absPath"] isDirectory:&isDir] && isDir)
		return 1;
	return 0;
}

- (BOOL)enabled
{
	[self willAccessValueForKey:@"enabled"];
	id value = [self primitiveValueForKey:@"enabled"];
	[self didAccessValueForKey:@"enabled"];
		
	return [value boolValue];
}

- (void)setEnabled:(BOOL)enabled
{
//	NSLog(@"set enabled to %d", enabled);
	
	[self willChangeValueForKey:@"enabled"];
	[self setPrimitiveValue:[NSNumber numberWithBool:enabled] forKey:@"enabled"];
	[self didChangeValueForKey:@"enabled"];
	
	if (enabled) {
		[[self valueForKey:@"parent"] setValue:[NSNumber numberWithBool:YES] forKey:@"enabled"];
	}
	else {
		NSEnumerator *childEnum = [[self valueForKey:@"children"] objectEnumerator];
		id child;
		while (child = [childEnum nextObject]) {
			[child setValue:[NSNumber numberWithBool:NO] forKey:@"enabled"];
		}
	}
}

@end
