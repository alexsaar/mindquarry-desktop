//
//  PathAbbreviation.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 20.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "PathAbbreviation.h"


@implementation PathAbbreviation

+ (Class)transformedValueClass 
{ 
	return [NSString class]; 
}

+ (BOOL)allowsReverseTransformation 
{ 
	return YES; 
}

- (id)transformedValue:(id)value 
{
	if ([value isKindOfClass:[NSString class]])
		return [value stringByAbbreviatingWithTildeInPath];
	return value;
}

- (id)reverseTransformedValue:(id)value
{
	if ([value isKindOfClass:[NSString class]])
		return [value stringByExpandingTildeInPath];
	return value;
}

@end
