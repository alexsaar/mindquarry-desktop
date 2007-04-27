//
//  MQChangeCell.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 15.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQChangeCell.h"
#import "MQTaskCell.h"
#import "MQChange.h"
#import "NSNumber+Additions.h"
#import "SVNController.h"

@implementation MQChangeCell

- (void)drawInteriorWithFrame:(NSRect)cellFrame inView:(NSView *)controlView
{
	BOOL isFocused = [[controlView window] isKeyWindow] && [[controlView window] firstResponder] == controlView;
	
	NSColor *textColor = [NSColor blackColor];
	NSColor *greenColor = [NSColor colorWithCalibratedRed:16 / 255.0 green:144 / 255.0 blue:0 alpha:1];
	NSColor *grayColor = [NSColor colorWithCalibratedWhite:0.5 alpha:1];
	NSColor *redColor = [NSColor colorWithCalibratedRed:200 / 255.0 green:16 / 255.0 blue:0 alpha:1];
	if ([self isHighlighted] && isFocused) {
		textColor = [NSColor colorWithCalibratedWhite:1 alpha:1.0];
		grayColor = [grayColor highlightWithLevel:0.5];
		greenColor = [greenColor highlightWithLevel:0.5];
		redColor = [redColor highlightWithLevel:0.5];
	}
	else if ([self isHighlighted]) {
		textColor = [NSColor colorWithCalibratedWhite:1 alpha:0.8];
		grayColor = [grayColor highlightWithLevel:0.7];
		greenColor = [greenColor highlightWithLevel:0.7];
		redColor = [redColor highlightWithLevel:0.7];

	}
	else {
		textColor = [NSColor blackColor];
//		grayColor = [NSColor colorWithCalibratedRed:0 green:0.5 blue:0 alpha:1];
	}
	
	NSMutableParagraphStyle *pstyle = [[[NSMutableParagraphStyle alloc] init] autorelease];
	[pstyle setLineBreakMode:NSLineBreakByTruncatingMiddle];
	
    // file size
    NSString *sizeString = nil;
    NSDictionary *sizeDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:9], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];
    float sizeWidth = 0;
	if ([[self objectValue] respondsToSelector:@selector(fileSize)]) {
		long filesize = [(MQChange *)[self objectValue] fileSize];
		if (filesize > 0) {
			sizeString = [[NSNumber numberWithLong:filesize] humanReadableFilesize];
            sizeWidth = [sizeString sizeWithAttributes:sizeDict].width + 5;
		}
	}
    
	// file path
	NSDictionary *titleDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:12], NSFontAttributeName, textColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];
	NSString *dpath = [[self objectValue] valueForKey:@"relPath"];
	NSSize titleSize = [dpath sizeWithAttributes:titleDict];
    float pathMaxWidth = cellFrame.size.width - 137 - sizeWidth;
	[dpath drawInRect:NSMakeRect(cellFrame.origin.x + 40, cellFrame.origin.y + 5, pathMaxWidth, 16) withAttributes:titleDict];
    
    [sizeString drawAtPoint:NSMakePoint(cellFrame.origin.x + 48 + MIN(titleSize.width, pathMaxWidth), cellFrame.origin.y + 8)  withAttributes:sizeDict];			
    
    [pstyle setLineBreakMode:NSLineBreakByTruncatingTail];
    
	// file icon
	NSString *path = [[self objectValue] valueForKey:@"absPath"];
	NSImage *icon = nil;
	if ([[NSFileManager defaultManager] fileExistsAtPath:path]) 
		icon = [[NSWorkspace sharedWorkspace] iconForFile:path];
	else
		icon = [[NSWorkspace sharedWorkspace] iconForFileType:[path pathExtension]];
	
	[icon setSize:NSMakeSize(32, 32)];
	[icon compositeToPoint:NSMakePoint(cellFrame.origin.x + 5, cellFrame.origin.y + 36) operation:NSCompositeSourceOver];
	
	// team name
	NSString *team = [[[self objectValue] valueForKey:@"team"] valueForKey:@"name"];
	NSDictionary *teamDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont boldSystemFontOfSize:9], NSFontAttributeName, greenColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];
	NSSize teamSize = [team sizeWithAttributes:teamDict];
	[team drawInRect:NSMakeRect(cellFrame.origin.x + 40, cellFrame.origin.y + 22, cellFrame.size.width - 45, 16) withAttributes:teamDict];

	// file kind
	if ([[self objectValue] respondsToSelector:@selector(fileKind)]) {
		NSDictionary *sizeDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:9], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];
		[[[self objectValue] fileKind] drawAtPoint:NSMakePoint(cellFrame.origin.x + 48 + teamSize.width, cellFrame.origin.y + 22)  withAttributes:sizeDict];
	}
	
	// status
	int status = [[[self objectValue] valueForKey:@"status"] intValue];
	NSValueTransformer *trans = [NSValueTransformer valueTransformerForName:@"StatusTransformer"];
	NSString *statusString = [[trans transformedValue:[[self objectValue] valueForKey:@"status"]] uppercaseString];
	NSColor *statusColor = textColor;
	if (status == SVN_STATUS_CONFLICTED || status == SVN_STATUS_MISSING)
		statusColor = redColor;
	else if (status > SVN_STATUS_MODIFIED)
		statusColor = greenColor;
	NSDictionary *statusDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont boldSystemFontOfSize:9], NSFontAttributeName, statusColor, NSForegroundColorAttributeName, nil];
	NSSize statusSize = [statusString sizeWithAttributes:statusDict];
	[statusString drawAtPoint:NSMakePoint(cellFrame.origin.x + cellFrame.size.width - statusSize.width - 45, cellFrame.origin.y + 8) withAttributes:statusDict];
	
	// action
	NSString *action = nil;
	BOOL enabled = [[[self objectValue] valueForKey:@"enabled"] boolValue];
	BOOL onServer = [[[self objectValue] valueForKey:@"onServer"] boolValue];
	BOOL local = [[[self objectValue] valueForKey:@"local"] boolValue];
//	BOOL upload = status == SVN_STATUS_UNVERSIONED || status == SVN_STATUS_ADDED;
	
	if (local && onServer)
		action = @"overwrite on server";
	else 
	if (onServer)
		action = @"download";
//	else if (status == SVN_STATUS_CONFLICTED)
//		action = @"overwrite";
//	else if (status == SVN_STATUS_MISSING)
//		action = @"delete";
	else if (local)
		action = @"upload to server";
//	else if (status > SVN_STATUS_MODIFIED)
//		action = @"whatever";
//	else
//		action = @"upload";
	
	if (!enabled)
		action = [NSString stringWithFormat:@"don't %@", action];
	
	action = [action uppercaseString];
	NSDictionary *actionDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:8], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, nil];
	NSSize actionSize = [action sizeWithAttributes:actionDict];
	[action drawAtPoint:NSMakePoint(cellFrame.origin.x + cellFrame.size.width - actionSize.width - 45, cellFrame.origin.y + 22) withAttributes:actionDict];

	// action icon
	NSImage *image = nil;
//	if (local && onServer)
//		image = [NSImage imageNamed:@"action-both"];
// 	else
	if (local)
		image = [NSImage imageNamed:@"action-up"];
	else if (onServer)
		image = [NSImage imageNamed:@"action-down"];
	
//	[image setSize:NSMakeSize(32, 32)];
	[image compositeToPoint:NSMakePoint(cellFrame.origin.x + cellFrame.size.width - 37, cellFrame.origin.y + 37) operation:NSCompositeSourceOver fraction:enabled ? 1.0 : 0.3];
	
}

- (id)objectValue
{
    return _change;
}

- (void)setObjectValue:(id)value
{
    if (NULL == value || value == _change)
        return;
    
	_change = value;
}

@end
