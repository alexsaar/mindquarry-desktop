
@class NSString;

#import <Growl/Growl.h>


// Growl Notification Types


// Tasks

// Indicates that the task list has been updated
static NSString * const GROWL_TASKS_UPDATED = @"Tasks updated";

// Indicates that a task has been save to the server
static NSString * const GROWL_TASK_SAVED = @"Task saved";


// Files

// Indicates that file synchronization has been completed
static NSString * const GROWL_FILES_SYNCHRONIZED = @"Files synchronized";

// Indicates that the files view has been updated
static NSString * const GROWL_FILE_STATUS_UPDATED = @"Files status updated";


