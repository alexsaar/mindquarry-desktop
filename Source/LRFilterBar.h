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

#import <Cocoa/Cocoa.h>

#import "LROverflowButton.h"
#import "LRFilterButton.h"


@interface LRFilterBar : NSView {
	
	@protected
	int buttonX;
	int xCoordinateOfViews;

	LROverflowButton *overflowButton;
	NSMenu *overflowMenu;
	
	NSArray *originalArray;
	SEL originalSelector;
	id originalSender;
	
	NSMutableArray *buttons;
	
	NSColor *topColor;
	NSColor *bottomColor;
}

#pragma mark Drawing
- (id)initWithFrame:(NSRect)frame;
- (void)drawRect:(NSRect)rect;
- (void)resizeSubviewsWithOldSize:(NSSize)oldBoundsSize;

#pragma mark Add Button
- (void)addItemsWithTitles:(NSArray *)array withSelector:(SEL)selector withSender:(id)sender;
- (void)addButtonWithTitle:(NSString *)title tag:(int)tag;

- (void) addDivider;
- (void) addLabel:(NSString*)label;

#pragma mark Accessor Methods
- (NSString *)getSelectedTitleInSegment:(int)seg;
- (int)getSelectedIndexInSegment:(int)seg;  // Kevin O. of CoolMacSoftware
- (int)getSelectedTagInSegment:(int)seg;
- (void)selectTag:(int)tag;

#pragma mark Coloring
- (NSColor *)topColor;
- (NSColor *)bottomColor;
- (void)setBlueBackground;
- (void)setGrayBackground;

@end

@interface LRFilterBar (Private)

- (NSArray*)allButtons;
- (void)_addItemsWithTitles:(NSArray *)array withSelector:(SEL)selector withSender:(id)sender;
- (LRFilterButton *)createButtonWithTitle:(NSString *)title;

- (void)removeAllItems; // Kevin O. of CoolMacSoftware
- (void)createOverflowMenu;
- (void)performActionForButton:(id)button;
- (void)deselectAllButtonsExcept:(id)button;

@end

