//
//  NSNumber+Additions.m
//  Mindquarry Desktop Client
//
//  Created by Jonas on 19.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "NSNumber+Additions.h"


@implementation NSNumber (Additions)

- (NSString *)humanReadableFilesize
{
	long long val = [self longLongValue];
	if (val == 1)
		return [NSString stringWithFormat:@"%d byte", val];
	if (val < 1024)
		return [NSString stringWithFormat:@"%d bytes", val];
	
	double fval = val / 1024.0;
	if (fval < 1024)
		return [NSString stringWithFormat:@"%.0f kbytes", fval];
	
	fval /= 1024.0;
	if (fval < 1024)
		return [NSString stringWithFormat:@"%.1f mbytes", fval];	
	
	fval /= 1024.0;
	if (fval < 1024)
		return [NSString stringWithFormat:@"%.2f gbytes", fval];
	
	fval /= 1024.0;
	return [NSString stringWithFormat:@"%.3f tbytes", fval];
}

@end
