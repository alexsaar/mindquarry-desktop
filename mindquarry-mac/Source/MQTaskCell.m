//
//  MQTaskCell.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "MQTaskCell.h"

#define _disabledFraction 0.50

static NSDictionary *statusImages;

@implementation MQTaskCell

+ (void)initialize
{
#define ICONSIZE NSMakeSize(28, 28)
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
	
	NSString *title = [[self objectValue] valueForKey:@"title"];
	[title drawAtPoint:NSMakePoint(cellFrame.origin.x + 36, cellFrame.origin.y + 6) withAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:12], NSFontAttributeName, textColor, NSForegroundColorAttributeName, nil]];
	
	NSString *status = [[self objectValue] valueForKey:@"status"];
	NSImage *statusImage = [statusImages objectForKey:status];
	if (!statusImage)
		statusImage = [statusImages objectForKey:@"task-new"];
	
	[statusImage compositeToPoint:NSMakePoint(cellFrame.origin.x + 5, cellFrame.origin.y + 30) operation:NSCompositeSourceOver];

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

