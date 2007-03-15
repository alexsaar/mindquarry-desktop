//
//  StatusColorTransformer.m
//  Mindquarry SVN
//
//  Created by Jonas on 12.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "StatusColorTransformer.h"


@implementation StatusColorTransformer

+ (Class)transformedValueClass 
{ 
	return [NSColor class]; 
}

+ (BOOL)allowsReverseTransformation 
{ 
	return NO; 
}

- (id)transformedValue:(id)value 
{
	if ([value intValue] == 9)
		return [NSColor redColor];
	else if ([value intValue] == 5)
		return [NSColor colorWithCalibratedRed:0 green:0.5 blue:0 alpha:1];
	return [NSColor blackColor];
}

@end
