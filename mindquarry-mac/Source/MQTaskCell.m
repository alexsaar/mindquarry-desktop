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

static NSDictionary *prioImages;

@implementation MQTaskCell

+ (void)initialize
{
#define ICONSIZE NSMakeSize(26, 26)
	statusImages = [[NSDictionary alloc] initWithObjectsAndKeys:
		MQSmoothResize([NSImage imageNamed:@"task-new"], ICONSIZE), @"new",
		MQSmoothResize([NSImage imageNamed:@"task-done"], ICONSIZE), @"done",
		MQSmoothResize([NSImage imageNamed:@"task-paused"], ICONSIZE), @"paused",
		MQSmoothResize([NSImage imageNamed:@"task-running"], ICONSIZE), @"running",
		nil];
	
#define PRIOSIZE NSMakeSize(16, 16)
	prioImages = [[NSDictionary alloc] initWithObjectsAndKeys:
		MQSmoothResize([NSImage imageNamed:@"task-low"], PRIOSIZE), @"low",
		MQSmoothResize([NSImage imageNamed:@"task-medium"], PRIOSIZE), @"medium",
		MQSmoothResize([NSImage imageNamed:@"task-important"], PRIOSIZE), @"important",
		MQSmoothResize([NSImage imageNamed:@"task-critical"], PRIOSIZE), @"critical",
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
	NSColor *greenColor = [NSColor colorWithCalibratedRed:16 / 255.0 green:144 / 255.0 blue:0 alpha:1];
	if ([self isHighlighted] && isFocused) {
		textColor = [NSColor colorWithCalibratedWhite:1 alpha:1.0];
		grayColor = [NSColor colorWithCalibratedWhite:1 alpha:0.7];
		greenColor = [greenColor highlightWithLevel:0.5];
	}
	else if ([self isHighlighted]) {
		textColor = [NSColor colorWithCalibratedWhite:1 alpha:0.8];
		grayColor = [NSColor colorWithCalibratedWhite:1 alpha:0.6];
		greenColor = [greenColor highlightWithLevel:0.7];
	}
	else {
		textColor = [NSColor blackColor];
		grayColor = [NSColor grayColor];
	}
	
	NSMutableParagraphStyle *pstyle = [[[NSMutableParagraphStyle alloc] init] autorelease];
	[pstyle setLineBreakMode:NSLineBreakByTruncatingTail];
	
	NSSize dueSize = NSMakeSize(0, 0);
	NSString *dateDesc = nil;
	NSDictionary *dateDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:10], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, nil];	
	NSDate *date = [[self objectValue] valueForKey:@"date"];
	if (date) {
		dateDesc = [[self objectValue] dueDescription];
		dueSize = [dateDesc sizeWithAttributes:dateDict];
	}
	// due	
	if (dateDesc) {
		[dateDesc drawAtPoint:NSMakePoint(cellFrame.origin.x + cellFrame.size.width - dueSize.width - 10, cellFrame.origin.y + 5) withAttributes:dateDict];
	}
	
	// title
	NSString *title = [[self objectValue] valueForKey:@"title"];
	NSDictionary *titleDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont systemFontOfSize:12], NSFontAttributeName, textColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];

	NSSize titleSize = [title sizeWithAttributes:titleDict];
	int maxSize = cellFrame.size.width - dueSize.width - 50;
	if (titleSize.width > maxSize)
		titleSize.width = maxSize;
	[title drawInRect:NSMakeRect(cellFrame.origin.x + 40, cellFrame.origin.y + 4, maxSize, 16) withAttributes:titleDict];
	
	NSString *team = [[[self objectValue] valueForKey:@"team"] valueForKey:@"name"];
	NSDictionary *teamDict = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont boldSystemFontOfSize:9], NSFontAttributeName, greenColor, NSForegroundColorAttributeName, pstyle, NSParagraphStyleAttributeName, nil];
	NSSize teamSize = [team sizeWithAttributes:teamDict];
	[team drawInRect:NSMakeRect(cellFrame.origin.x + 42, cellFrame.origin.y + 21, cellFrame.size.width - 55, 16) withAttributes:teamDict];	
	
	// summary
	NSString *sum = [[self objectValue] valueForKey:@"summary"];
	if (sum) {
		NSMutableString *summary = [NSMutableString stringWithString:sum];
		[summary replaceOccurrencesOfString:@"\n" withString:@" " options:0 range:NSMakeRange(0, [summary length])];
		
		[summary drawInRect:NSMakeRect(cellFrame.origin.x + 50 + teamSize.width, cellFrame.origin.y + 21, cellFrame.size.width - 61 - teamSize.width, 14) withAttributes:[NSDictionary dictionaryWithObjectsAndKeys:pstyle, NSParagraphStyleAttributeName, [NSFont systemFontOfSize:10], NSFontAttributeName, grayColor, NSForegroundColorAttributeName, nil]];
	}
	
	// status icon
	NSString *status = [[self objectValue] valueForKey:@"status"];
	NSImage *statusImage = [statusImages objectForKey:status];
	if (!statusImage)
		statusImage = [statusImages objectForKey:@"new"];
	[statusImage compositeToPoint:NSMakePoint(cellFrame.origin.x + 5, cellFrame.origin.y + 34) operation:NSCompositeSourceOver];
	
	// prio icon
	NSString *prio = [[self objectValue] valueForKey:@"priority"];
	NSImage *prioImage = [prioImages objectForKey:prio];
	if (prioImage)
		[prioImage compositeToPoint:NSMakePoint(cellFrame.origin.x + 21, cellFrame.origin.y + 36) operation:NSCompositeSourceOver];	
	
	// needs update?
	BOOL needsUpdate = [[[self objectValue] valueForKey:@"needsUpdate"] boolValue];
	if (needsUpdate) {
		NSString *needsUpdateString = @"Has local changes";
		NSDictionary *attr = [NSDictionary dictionaryWithObjectsAndKeys:[NSFont boldSystemFontOfSize:9], NSFontAttributeName, [NSColor redColor], NSForegroundColorAttributeName, nil];
		NSSize needsUpdateSize = [needsUpdateString sizeWithAttributes:attr];
		[needsUpdateString drawAtPoint:NSMakePoint(cellFrame.origin.x + cellFrame.size.width - needsUpdateSize.width - 10, cellFrame.origin.y + 22) withAttributes:attr];
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

