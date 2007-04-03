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
	if (val == SVN_STATUS_UNVERSIONED || val == SVN_STATUS_ADDED) 
		return @"added";
	else if (val == SVN_STATUS_MODIFIED)
		return @"modified";
	else if (val == SVN_STATUS_MISSING || val == SVN_STATUS_DELETED)
		return @"deleted";
	else if (val == SVN_STATUS_CONFLICTED)
		return @"conflict";
	return [NSString stringWithFormat:@"code: %@", value];
}

@end
