//
//  StatusTransformer.m
//  Mindquarry SVN
//
//  Created by Jonas on 12.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "StatusTransformer.h"

#import "SVNController.h"

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
	int val = [value intValue];
	if (val == 5 || val == 3) 
		return @"new: local";
	else if (val == SVN_STATUS_DOWNLOAD)
		return @"from server";
	else if (val == 2)
		return @"modified";
	else if (val == 6)
		return @"deleted";
	else if (val == 9)
		return @"conflict";
	return [NSString stringWithFormat:@"code: %@", value];
}

@end
