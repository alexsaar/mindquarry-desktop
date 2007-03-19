//
//  StatusTransformer.m
//  Mindquarry SVN
//
//  Created by Jonas on 12.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "StatusTransformer.h"


@implementation StatusTransformer

+ (Class)transformedValueClass 
{ 
	return [NSString class]; 
}

+ (BOOL)allowsReverseTransformation 
{ 
	return NO; 
}

- (id)transformedValue:(id)value 
{
	if ([value intValue] == 5 || [value intValue] == 3) 
		return @"new";
	else if ([value intValue] == 2)
		return @"modified";
	else if ([value intValue] == 6)
		return @"deleted";
	else if ([value intValue] == 9)
		return @"conflict";
	return [NSString stringWithFormat:@"code: %@", value];
}

@end
