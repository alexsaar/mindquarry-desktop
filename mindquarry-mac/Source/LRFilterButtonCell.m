/*
 Copyright (c) 2006 by Logan Design, http://www.burgundylogan.com/
 
 Permission is hereby granted, free of charge, to any person obtaining a
 copy of this software and associated documentation files (the "Software"),
 to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense,
 and/or sell copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 THE COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

#import "LRFilterBar.h"
#import "LRFilterButtonCell.h"
#import "CTGradient.h"

static NSShadow *whiteShadow, *darkShadow;

@implementation LRFilterButtonCell

+ (void) initialize
{
	// initializing shadows
	whiteShadow = [[NSShadow alloc] init];
	[whiteShadow setShadowOffset:NSMakeSize(0.0, -1.0)];
	[whiteShadow setShadowBlurRadius:1.0];
	[whiteShadow setShadowColor:[NSColor colorWithCalibratedWhite:0.3 alpha:1.0]];

	darkShadow = [[NSShadow alloc] init];
	[darkShadow setShadowOffset:NSMakeSize(0.0, -1.0)];
	[darkShadow setShadowBlurRadius:1.0];
	[darkShadow setShadowColor:[NSColor controlDarkShadowColor]];
}

- (id) init
{
	if (![super init])
		return nil;
	
	mouseInView = FALSE;
	
	return self;
}

#pragma mark Drawing

- (void)drawBezelWithFrame:(NSRect)frame inView:(NSView*)controlView
{
	if ([self state] == NSOnState || mouseInView)
		[super drawBezelWithFrame:frame inView:controlView];	
}

- (void)drawInteriorWithFrame:(NSRect)cellFrame inView:(NSView *)controlView
{
	NSShadow *shadow = whiteShadow;
	NSColor	*color = [NSColor blackColor];
	if (mouseInView || [self state] == NSOnState) {
		shadow = darkShadow;
		color = [NSColor whiteColor];
	}
	
	NSDictionary *stringAttributes = [NSDictionary dictionaryWithObjectsAndKeys:
		[NSFont systemFontOfSize:12], NSFontAttributeName, color, NSForegroundColorAttributeName,  NULL];
	
	[self drawBezelWithFrame:cellFrame inView:controlView];
	[[self title] drawInRect:NSMakeRect(cellFrame.origin.x + 8, cellFrame.origin.y , cellFrame.size.width, cellFrame.size.height)
			  withAttributes:stringAttributes];
}

- (void)mouseEntered:(NSEvent *)event
{
	mouseInView = YES;
	[super mouseEntered:event];
}

- (void)mouseExited:(NSEvent *)event
{
	mouseInView = NO;
	[super mouseExited:event];
}

@end

