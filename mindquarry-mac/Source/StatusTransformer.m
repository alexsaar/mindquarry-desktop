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
	switch ([value intValue]) {
		
		case SVN_STATUS_NONE:
			return @"none";
		
		case SVN_STATUS_NORMAL:
			return @"normal";

		case SVN_STATUS_MODIFIED:
			return @"modified";
			
		case SVN_STATUS_ADDED:
		case SVN_STATUS_UNVERSIONED:
			return @"added";
		
		case SVN_STATUS_DELETED:
		case SVN_STATUS_MISSING:
			return @"deleted";
			
		case SVN_STATUS_REPLACED:
			return @"replaced";
		
		case SVN_STATUS_MERGED:
			return @"merged";
			
		case SVN_STATUS_CONFLICTED:
			return @"conflicted";
		
		case SVN_STATUS_OBSTRUCTED:
			return @"obstructed";
		
		case SVN_STATUS_IGNORED:
			return @"ignored";
			
		case SVN_STATUS_INCOMPLETE:
			return @"incomplete";
	
		case SVN_STATUS_EXTERNAL:
			return @"external";
			
		default:
			return [NSString stringWithFormat:@"code: %@", value];
	}
}

@end
