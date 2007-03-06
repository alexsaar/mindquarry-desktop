#import <Cocoa/Cocoa.h>


@interface OFGradientTableView : NSTableView {

}

@end

@interface OFGradientTableView (Private)

- (void)_windowDidChangeKeyNotification:(NSNotification *)notification;

@end