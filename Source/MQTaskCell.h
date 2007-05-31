//
//  MQTaskCell.h
//  Mindquarry Desktop Client
//
//  Created by Jonas on 06.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface MQTaskCell : NSCell {

	id _task;
	
}

@end

NSImage *MQSmoothResize(NSImage *source, NSSize size);
