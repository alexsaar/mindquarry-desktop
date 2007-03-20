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
#import "CTGradient.h"
#import "LRFilterButtonCell.h"


#define kLeftMargin 5
#define kSpacing 2

@implementation LRFilterBar

- (id)initWithFrame:(NSRect)frame
{
	if (![super initWithFrame:frame])
		return nil;

	[self setAutoresizesSubviews:YES];
	buttonX = kLeftMargin;

	// Create Button Arrays
	buttons = [[NSMutableArray alloc] init];
	[buttons addObject:[[[NSMutableArray alloc] init] autorelease]];

	// Create Overflow Button
	overflowButton = [[LROverflowButton alloc] init];
	[overflowButton setTarget:self];
	
	// Create Colors
	topColor = NULL;
	bottomColor = NULL;
	
	// Set Default Color
	[self setBlueBackground];
	
	return self;
}

//- (void)awakeFromNib
//{
//	// add search field
//	NSSearchField *field = [[NSSearchField alloc] init];
//	[field setAutoresizingMask:NSViewMinXMargin | NSViewMinYMargin];
//	[field setFrameOrigin:NSMakePoint([self bounds].size.width - 110, 1)];
//	[field setFrameSize:NSMakeSize(100, 19)];
//	[[field cell] setControlSize:NSSmallControlSize];
//	[self addSubview:field];
//	[field release];
//}

- (void) dealloc
{
	[overflowButton release];
	[overflowMenu release];
	[originalArray release];
	[buttons release];
	[topColor release];
	[bottomColor release];

	[super dealloc];
}

#pragma mark Drawing

- (void)drawRect:(NSRect)rect
{
	if (topColor && bottomColor) {
		CTGradient *aGradient = [CTGradient gradientWithBeginningColor:topColor endingColor:bottomColor];
		[aGradient fillRect:rect angle:90];
	}
	
	[[NSColor blackColor] set];
	[NSBezierPath strokeLineFromPoint:NSMakePoint(0, 0) toPoint:NSMakePoint(rect.size.width, 0)];
}

- (NSArray*)allButtons
{
	NSMutableArray *result = [NSMutableArray array];
	NSEnumerator *enumerator = [buttons objectEnumerator];
	id object;
	
	while (object = [enumerator nextObject]) {	
		[result addObjectsFromArray:object];
	}
	
	return result;
}

- (void)resizeSubviewsWithOldSize:(NSSize)oldBoundsSize
{
	NSMutableArray *selectedTitleArray = [[[NSMutableArray alloc] init] autorelease];	
	
	NSEnumerator *subenumerator, *enumerator = [buttons objectEnumerator];
	id object, dictObject;
	
	while (dictObject = [enumerator nextObject]) {
		subenumerator = [dictObject objectEnumerator];
		
		while (object = [subenumerator nextObject]) {
			if ([object respondsToSelector:@selector(state)] && [object state] == NSOnState) {
				[selectedTitleArray addObject:[NSNumber numberWithInt:[object tag]]];
			}

			if ([object class] != [NSMenuItem class]) {
				[object removeFromSuperview];
			}
		}
				
		[dictObject removeAllObjects];
		buttonX = kLeftMargin;
		
		if(overflowButton) {
			[overflowButton removeFromSuperview];
			[overflowMenu release];
		}
	}
	
	[buttons removeAllObjects];
	[buttons addObject:[NSMutableArray array]];
	
	if (originalArray) {
		[self _addItemsWithTitles:originalArray withSelector:originalSelector withSender:originalSender];
	}
	
	enumerator = [[self allButtons] objectEnumerator];
	while (object = [enumerator nextObject]) {
		if([selectedTitleArray containsObject:[NSNumber numberWithInt:[object tag]]] &&[object respondsToSelector:@selector(setState:)]) {
			[object setState:1];
		}
	}
}

#pragma mark Add Button

- (void)addItemsWithTitles:(NSArray *)array withSelector:(SEL)selector withSender:(id)sender
{
	[self _addItemsWithTitles:array withSelector:selector withSender:sender];
}

- (void)_addItemsWithTitles:(NSArray *)array withSelector:(SEL)selector withSender:(id)sender
{	
	int i, count = [array count], tag = 0;
	for(i = 0; i < count; i++) {
		
		if ([[array objectAtIndex:i] isEqualTo:@"DIVIDER"]) {
			[self addDivider];
		} else if ([[array objectAtIndex:i] hasPrefix:@"LABEL:"]) {
			NSRange range = [[array objectAtIndex:i] rangeOfString:@"LABEL:"];
			[self addLabel: [[array objectAtIndex:i] substringFromIndex:range.location + range.length]];
		} else {
			[self addButtonWithTitle:[array objectAtIndex:i] tag:tag];
			tag++;
		}
	}
	
	if (!originalArray) {
		originalArray = [[NSArray alloc] initWithArray:array];
		originalSelector = selector;
		originalSender = sender;
	}
}

- (void)addButtonWithTitle:(NSString *)title tag:(int)tag
{
	LRFilterButton *newButton = [[self createButtonWithTitle:title] retain];
	
	[newButton setAction:@selector(performActionForButton:)];
	[newButton setTarget:self];
	[newButton setTag:tag];
	
	// Set X,Y Coordinates
	int buttonHeight = [newButton frame].size.height;
	int viewHeight = [self frame].size.height;
	int buttonYCoordinate = (viewHeight-buttonHeight) / 2;
	
	int buttonXCoordinate = buttonX;
	
	[newButton setFrameOrigin:NSMakePoint(buttonXCoordinate,buttonYCoordinate)];
	
	// Increment the X Offset For Next Button
	buttonX += [newButton frame].size.width + kSpacing;
	
	// Add To View
	if( buttonX < [self frame].size.width - [overflowButton frame].size.width ) {
		[self addSubview:newButton];
		
		[[buttons objectAtIndex:[buttons count] - 1] addObject:newButton];
		[newButton setShowsBorderOnlyWhileMouseInside:TRUE];
	} else {
		if( !overflowMenu )
			[self createOverflowMenu];
		
		NSMenuItem *newMenuItem = [[NSMenuItem alloc] initWithTitle:title action:@selector(performActionForButton:) keyEquivalent:@""];
		[newMenuItem setTarget:self];
		[newMenuItem setTag:tag];
		[overflowMenu addItem:newMenuItem];
		
		[[buttons objectAtIndex:[buttons count] - 1] addObject:newMenuItem];
	}
	
	[newButton release];
}

- (LRFilterButton *)createButtonWithTitle:(NSString *)title
{
	// Create Button
	LRFilterButton *newButton = [[[LRFilterButton alloc] init] autorelease];
	[newButton setTitle:title];
	[newButton sizeToFit];
	[newButton setFocusRingType:NSFocusRingTypeNone];
	[newButton setShowsBorderOnlyWhileMouseInside:FALSE];
	
	return newButton;
}

- (void) addLabel:(NSString*)label
{
	if( buttonX < [self frame].size.width) {
		NSTextField *labelField = [[[NSTextField alloc] initWithFrame:NSMakeRect(0, 0, 0, 0)] autorelease];
		[labelField setObjectValue:label];
		[labelField setDrawsBackground:FALSE];
		[labelField setSelectable:FALSE];
		[labelField setBordered:FALSE];
		[labelField setEditable:FALSE];
		[labelField setTextColor:[NSColor grayColor]];
		[labelField setFocusRingType:NSFocusRingTypeNone];
		[[labelField cell] setFont:[NSFont systemFontOfSize:[NSFont smallSystemFontSize]]];
		
		// calculating the size
		NSSize buttonSize = [[labelField cell] cellSizeForBounds: NSMakeRect(0,0,2000,2000)];
		
		int buttonYCoordinate = (([self frame].size.height - buttonSize.height) / 2);
		[labelField setFrameOrigin:NSMakePoint(buttonX,buttonYCoordinate + 1)];
		[labelField setFrameSize:buttonSize];

		buttonX += buttonSize.width + kSpacing;
		
		[self addSubview:labelField];
		
		[[buttons objectAtIndex:[buttons count] - 1] addObject:labelField];
	}
}

- (void) addDivider
{
	if( buttonX < [self frame].size.width - [overflowButton frame].size.width ) {
		
		buttonX += 3;
		
		NSButton *newButton = [[[NSButton alloc] init] autorelease];
		[newButton setImage:[NSImage imageNamed:@"OverflowDivider.png"]];
		[newButton setBordered:NO];
		[newButton sizeToFit];
		
		// Set X,Y Coordinates
		int buttonHeight = [newButton frame].size.height;
		int viewHeight = [self frame].size.height;
		int buttonYCoordinate = (viewHeight-buttonHeight) / 2;

		[newButton setFrameOrigin:NSMakePoint(buttonX,buttonYCoordinate)];
		
		// Increment the X Offset For Next Button
		buttonX += [newButton frame].size.width + 3 + kSpacing;
		
		// Add To View
		[self addSubview:newButton];
		[[buttons objectAtIndex:[buttons count] - 1] addObject:newButton];
	} else {
		
		if( !overflowMenu )
			[self createOverflowMenu];
		
		NSMenuItem *newMenuItem = [NSMenuItem separatorItem];
		[overflowMenu addItem:newMenuItem];
		
		[[buttons objectAtIndex:[buttons count] - 1] addObject:newMenuItem];
	}
	
	[buttons addObject:[[[NSMutableArray alloc] init] autorelease]];
}


#pragma mark Remove All

- (void)removeAllItems // Kevin O. of CoolMacSoftware
{	
	NSEnumerator *dictEnumerator = [buttons objectEnumerator];
	id dictObject;

	while( dictObject = [dictEnumerator nextObject] ) {
		
		// Remove Old Buttons
		NSEnumerator *enumerator = [dictObject objectEnumerator];
		id object;
		
		while ( object = [enumerator nextObject] ) {
			
			if( [object class] != [NSMenuItem class] )
				[object removeFromSuperview];
		}
		
		[dictObject removeAllObjects];
		
		// Remove Overflow Button
		if(overflowButton) {
			[overflowButton removeFromSuperview];
			[overflowMenu release];
		}
	}
	
	[buttons removeAllObjects];
	[buttons addObject:[[NSMutableArray alloc] init]];

	[originalArray release];
	originalArray = nil;
	
	originalSelector = NULL;
	originalSender = NULL;
	buttonX = kLeftMargin;
}

#pragma mark Overflow Menu

- (void)createOverflowMenu
{
	// Create Menu
	overflowMenu = [[NSMenu alloc] init];
	[overflowButton setMenu:overflowMenu];
	
	// Set X,Y Coordinates
	int buttonHeight = [overflowButton frame].size.height;
	int viewHeight = [self frame].size.height;
	int buttonWidth = [overflowButton frame].size.width;
	int viewWidth = [self frame].size.width;
	
	int buttonYCoordinate = (viewHeight-buttonHeight) / 2;
	int buttonXCoordinate = viewWidth-buttonWidth;
	
	[overflowButton setFrameOrigin:NSMakePoint(buttonXCoordinate,buttonYCoordinate)];
	
	// Add Subview Button
	[self addSubview:overflowButton];
}

#pragma mark Button Action

- (void)performActionForButton:(id)button
{
	[self deselectAllButtonsExcept:button];
	[originalSender performSelector:originalSelector withObject:button];
}

#pragma mark Deselect

- (void)deselectAllButtonsExcept:(id)button
{
	NSEnumerator *dictEnumerator = [buttons objectEnumerator];
	id dictObject;
	
	while (dictObject = [dictEnumerator nextObject]) {
		
		if ([dictObject containsObject:button]) {
			
			NSEnumerator *e = [dictObject objectEnumerator];
			id object;
			
			while (object = [e nextObject]) {
				if ([object respondsToSelector:@selector(setState:)]) {
					[object setState:NSOffState];
				}
			}
			
			if ([button respondsToSelector:@selector(setState:)]) {
				[button setState:NSOnState];
			}
		}
	}
}

#pragma mark Accessor Methods

- (NSString *)getSelectedTitleInSegment:(int)seg
{
	if( seg < [buttons count] ) {
		NSEnumerator *e = [[buttons objectAtIndex:seg] objectEnumerator];
		id object;
		
		while ( object = [e nextObject] ) {
			if([object respondsToSelector:@selector(state:)] && [object state] == NSOnState)
				return [object title];
		}
	}
	
	return @"";
}

- (int)getSelectedIndexInSegment:(int)seg  // Kevin O. of CoolMacSoftware
{
	int index = 0;
	
	if( seg < [buttons count] ) {
		
		NSEnumerator *e = [[buttons objectAtIndex:seg] objectEnumerator];
		id object;
		
		while (object = [e nextObject]) {
			if ([object respondsToSelector:@selector(state:)] && [object state] == NSOnState)
				return index;

			index++;
		}
	}
	
	return -1;
}

- (int)getSelectedTagInSegment:(int)seg
{
	if ([buttons count] > seg) {
		NSArray *segment = [buttons objectAtIndex:seg];
		int index = [self getSelectedIndexInSegment:seg];
		
		if (index >= 0 && [segment count] > index) {
			
			return [[segment objectAtIndex:index] tag];
		}
	}

	return -1;
}

- (void)selectTag:(int)tag
{
	NSArray *subbuttons;
	int i, j, count = [buttons count];

	for (i = 0; i < count; i++) {
		subbuttons = [buttons objectAtIndex:i];
		for (j = 0; j < [subbuttons count]; j++) {
			if ([[subbuttons objectAtIndex:j] respondsToSelector:@selector(setState:)]) {
				if([[subbuttons objectAtIndex:j] tag] == tag) {
					[[subbuttons objectAtIndex:j] setState:NSOnState];
				} else {
					[[subbuttons objectAtIndex:j] setState:NSOffState];
				}
			}
		}
	}
}

- (NSColor *)topColor
{
	return topColor;
}

- (NSColor *)bottomColor
{
	return bottomColor;
}

- (void)setBlueBackground
{
	[topColor release];
	topColor = [[NSColor colorWithCalibratedRed:(182.0/255.0) green:(192.0/255.0) blue:(207.0/255.0) alpha:1.0] retain];
	[bottomColor release];
	bottomColor = [[NSColor colorWithCalibratedRed:(203.0/255.0) green:(210.0/255.0) blue:(221.0/255.0) alpha:1.0] retain];
	[self setNeedsDisplay:TRUE];
}

- (void)setGrayBackground
{
	[topColor release];
	topColor = [[NSColor colorWithCalibratedRed:(181.0/255.0) green:(181.0/255.0) blue:(181.0/255.0) alpha:1.0] retain];
	[bottomColor release];
	bottomColor = [[NSColor colorWithCalibratedRed:(216.0/255.0) green:(216.0/255.0) blue:(216.0/255.0) alpha:1.0] retain];
	[self setNeedsDisplay:TRUE];
}

- (BOOL)isOpaque
{
	return YES;
}

@end
