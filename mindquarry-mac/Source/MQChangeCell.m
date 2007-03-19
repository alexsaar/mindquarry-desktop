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
	[pstyle setLineBreakMode:NSLineBreakByTruncatingTail];
	
	NSDictionary *titleDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:12], NSFontAttributeName, textColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];
	NSString *dpath = [[self objectValue] valueForKey:@"relPath"];
	NSSize titleSize = [dpath sizeWithAttributes:titleDict];
	[dpath drawInRect:NSMakeRect(cellFrame.origin.x + 40, cellFrame.origin.y + 5, cellFrame.size.width - 45, 16) withAttributes:titleDict];
	
	if ([[self objectValue] respondsToSelector:@selector(fileSize)]) {
		long filesize = [(MQChange *)[self objectValue] fileSize];
		if (filesize > 0) {
			NSString *sizeString = [[NSNumber numberWithLong:filesize] humanReadableFilesize];
			NSDictionary *sizeDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:9], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];
			[sizeString drawAtPoint:NSMakePoint(cellFrame.origin.x + 48 + titleSize.width, cellFrame.origin.y + 8)  withAttributes:sizeDict];			
		}
	}
	
	NSString *path = [[self objectValue] valueForKey:@"absPath"];
	NSImage *icon = [[NSWorkspace sharedWorkspace] iconForFile:path];
	[icon setSize:NSMakeSize(32, 32)];
	[icon compositeToPoint:NSMakePoint(cellFrame.origin.x + 5, cellFrame.origin.y + 36) operation:NSCompositeSourceOver];
	
	NSString *team = [[[self objectValue] valueForKey:@"team"] valueForKey:@"name"];
	NSDictionary *teamDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont boldSystemFontOfSize:9], NSFontAttributeName, greenColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];
	[team drawInRect:NSMakeRect(cellFrame.origin.x + 40, cellFrame.origin.y + 22, cellFrame.size.width - 45, 16) withAttributes:teamDict];

	NSValueTransformer *trans = [NSValueTransformer valueTransformerForName:@"StatusTransformer"];
	int status = [[[self objectValue] valueForKey:@"status"] intValue];
	NSString *statusString = [[trans transformedValue:[[self objectValue] valueForKey:@"status"]] uppercaseString];
	NSColor *statusColor = textColor;
	if (status == 9 || status == 6)
		statusColor = redColor;
	else if (status > 2)
		statusColor = greenColor;
	NSDictionary *statusDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont boldSystemFontOfSize:9], NSFontAttributeName, statusColor, NSForegroundColorAttributeName, nil];
	NSSize statusSize = [statusString sizeWithAttributes:statusDict];
	[statusString drawAtPoint:NSMakePoint(cellFrame.origin.x + cellFrame.size.width - statusSize.width - 5, cellFrame.origin.y + 14) withAttributes:statusDict];

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
