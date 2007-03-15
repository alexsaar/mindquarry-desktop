//
//  IconTransformer.m
//  Mindquarry SVN
//
//  Created by Jonas on 12.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "IconTransformer.h"


@implementation IconTransformer

+ (Class)transformedValueClass 
{ 
	return [NSImage class]; 
}

+ (BOOL)allowsReverseTransformation 
{ 
	return NO; 
}

- (id)transformedValue:(id)value 
{
	return [[NSWorkspace sharedWorkspace] iconForFile:value];
}

@end
