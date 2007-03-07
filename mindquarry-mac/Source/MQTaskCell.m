//
//  MQTaskCell.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTaskCell.h"

#import "MQTask.h"

#define _disabledFraction 0.50

static NSDictionary *statusImages;

@implementation MQTaskCell

+ (void)initialize
{
#define ICONSIZE NSMakeSize(32, 32)
	statusImages = [[NSDictionary alloc] initWithObjectsAndKeys:
		MQSmoothResize([NSImage imageNamed:@"task-new"], ICONSIZE), @"new",
		MQSmoothResize([NSImage imageNamed:@"task-done"], ICONSIZE), @"done",
		MQSmoothResize([NSImage imageNamed:@"task-paused"], ICONSIZE), @"paused",
		MQSmoothResize([NSImage imageNamed:@"task-running"], ICONSIZE), @"running",
		nil];
}

- (id)objectValue
{
    return _task;
}

- (void)setObjectValue:(id)value
{
    if (NULL == value || value == _task)
        return;
    
	_task = value;
}


- (void)drawInteriorWithFrame:(NSRect)cellFrame inView:(NSView *)controlView
{
	BOOL isFocused = [[controlView window] isKeyWindow] && [[controlView window] firstResponder] == controlView;
	
	NSColor *textColor = NULL, *grayColor = NULL;
	if ([self isHighlighted] && isFocused) {
		textColor = [NSColor colorWithCalibratedWhite:1 alpha:1.0];
		grayColor = [NSColor colorWithCalibratedWhite:1 alpha:0.7];
	}
	else if ([self isHighlighted]) {
		textColor = [NSColor colorWithCalibratedWhite:1 alpha:0.8];
		grayColor = [NSColor colorWithCalibratedWhite:1 alpha:0.6];
	}
	else {
		textColor = [NSColor blackColor];
		grayColor = [NSColor grayColor];
	}
	
	// title
	NSString *title = [[self objectValue] valueForKey:@"title"];
	NSDictionary *titleDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:12], NSFontAttributeName, textColor, NSForegroundColorAttributeName, nil];
	NSSize titleSize = [title sizeWithAttributes:titleDict];
	[title drawAtPoint:NSMakePoint(cellFrame.origin.x + 40, cellFrame.origin.y + 4) withAttributes:titleDict];
	
	// summary
	NSString *sum = [[self objectValue] valueForKey:@"summary"];
	if (sum) {
		NSMutableString *summary = [NSMutableString stringWithString:sum];
		[summary replaceOccurrencesOfString:@"\n" withString:@" " options:0 range:NSMakeRange(0, [summary length])];
		[summary drawAtPoint:NSMakePoint(cellFrame.origin.x + 44, cellFrame.origin.y + 21) withAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:10], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, nil]];		
	}
	
	// status icon
	NSString *status = [[self objectValue] valueForKey:@"status"];
	NSImage *statusImage = [statusImages objectForKey:status];
	if (!statusImage)
		statusImage = [statusImages objectForKey:@"new"];
	
	[statusImage compositeToPoint:NSMakePoint(cellFrame.origin.x + 5, cellFrame.origin.y + 37) operation:NSCompositeSourceOver];

	
	// due
	NSDate *date = [[self objectValue] valueForKey:@"date"];
	if (date) {
		NSString *dateDesc = [NSString stringWithFormat:@" - %@", [[self objectValue] dueDescription]];
		NSDictionary *dateDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:10], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, nil];
//		NSSize dateSize = [dateDesc sizeWithAttributes:dateDict];
		[dateDesc drawAtPoint:NSMakePoint(cellFrame.origin.x + titleSize.width + 42, cellFrame.origin.y + 6) withAttributes:dateDict];
		
//		[@"due:" drawAtPoint:NSMakePoint(cellFrame.origin.x + cellFrame.size.width - 35 - dateSize.width, cellFrame.origin.y + 10) withAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:10], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, nil]];
	}
	
}

@end

NSImage *MQSmoothResize(NSImage *source, NSSize size)
{
	NSImage *i2;	
	NSImageRep *bestRep = NULL;
	float bestRepSize = 0;
	
	NSEnumerator *e = [[source representations] objectEnumerator];
	NSImageRep *rep;
	
	while (rep = [e nextObject]) {
		NSSize s = [rep size];
		if (s.width * s.height > bestRepSize) {
			bestRep = rep;
			bestRepSize = s.width * s.height;
		}
	}
	
	NSRect imgRect = NSMakeRect(0, 0, size.width, size.height);
	i2 = [[NSImage alloc] initWithSize:imgRect.size];
	
	[i2 lockFocus];
	[[NSGraphicsContext currentContext] setImageInterpolation:NSImageInterpolationHigh];
	[bestRep drawInRect:imgRect];
	[i2 unlockFocus];
    
	return [i2 autorelease];
}

