//
//  SVNController.m
//  Mindquarry SVN
//
//  Created by Jonas on 12.03.07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "SVNController.h"

#import "JNIHelper.h"
#import "JVMController.h"

@implementation SVNController

- (id)init 
{
	if (![super init])
		return nil;
	
	BOOL attached = NO;
	
	if ([JVMController JavaVM]) {
		[self attachCurrentThread];
		attached = YES;
	}
	
	[JVMController createJVMIfNeeded];

	if (!attached)
		[self attachCurrentThread];
	
	CHECK_EXCEPTION;
	
	helperClass = env->FindClass("com/mindquarry/desktop/workspace/MacSVNHelper");
	if (!helperClass) {
		NSLog(@"could not find MacSVNHelper class");
		return nil;
	}
	
	CHECK_EXCEPTION;
		
	return self;
}

- (id)initWithRepository:(NSString *)_repo username:(NSString *)_user password:(NSString *)_pass localPath:(NSString *)_local
{
	if (![self init])
		return nil;
	
	[self setValue:_repo forKey:@"repository"];
	[self setValue:_user forKey:@"username"];
	[self setValue:_pass forKey:@"password"];
	[self setValue:_local forKey:@"localPath"];
		
	jmethodID helperConstructor = env->GetMethodID(helperClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
	CHECK_EXCEPTION;
	if (!helperConstructor) {
		NSLog(@"warning: could not find helper constructor");
		return nil;
	}
	
	jobject helper = env->NewObject(helperClass, helperConstructor, 
									nsstring_to_jstring(env, _repo),
									nsstring_to_jstring(env, _local),
									nsstring_to_jstring(env, _user),
									nsstring_to_jstring(env, _pass), nil);
	CHECK_EXCEPTION;
	if (!helper) {
		NSLog(@"warning: could not init helper ");
		return nil;
	}
	
	helperRef = env->NewGlobalRef(helper);
	
	CHECK_EXCEPTION;
	
	return self;
}

- (void)dealloc
{
	env->DeleteGlobalRef(helperRef);
	CHECK_EXCEPTION;
	helperRef = nil;
	
	[super dealloc];
}

- (void)attachCurrentThread
{
	[JVMController JavaVM]->AttachCurrentThread((void **)&env, NULL);
}

- (void)setJavaEnv:(JNIEnv *)_env
{
	env = _env;
}

- (BOOL)updateReturnError:(NSError **)error
{
	static jmethodID updateMethod = nil;
	if (!updateMethod) {
		updateMethod = env->GetMethodID(helperClass, "update", "()V");
		CHECK_EXCEPTION;
		if (!updateMethod) {
			NSLog(@"Warning: could not get update method ID");
			return NO;
		}
	}
	
	env->CallVoidMethod(helperRef, updateMethod);
	CHECK_EXCEPTION;
	
	if (env->ExceptionCheck() == JNI_TRUE) {
		env->ExceptionDescribe();
		env->ExceptionClear();
		return NO;
	}
	
	return YES;
}

- (BOOL)getLocalChanges:(NSMutableArray **)changes returnError:(NSError **)error
{
	static jmethodID localChangesMethod = nil;
	if (!localChangesMethod) {
		localChangesMethod = env->GetMethodID(helperClass, "getLocalChanges", "()[Lorg/tigris/subversion/javahl/Status;");
		CHECK_EXCEPTION;
		if (!localChangesMethod) {
			NSLog(@"Warning: could not get localChanges method ID");
			return NO;
		}
	}
	
	jobjectArray changesArray = (jobjectArray) env->CallObjectMethod(helperRef, localChangesMethod);
		
	if (env->ExceptionCheck() == JNI_TRUE) {
		env->ExceptionDescribe();
		env->ExceptionClear();
		return NO;
	}
	
	jsize len = env->GetArrayLength(changesArray);
	
	static jclass statusClass = nil;
	if (!statusClass) {
		statusClass = env->FindClass("org/tigris/subversion/javahl/Status");
		CHECK_EXCEPTION;
		if (!statusClass) {
			NSLog(@"Warning: could not find status class");
			return NO;
		}
	}
	
	static jfieldID textStatusField = nil;
	if (!textStatusField) {
		textStatusField = env->GetFieldID(statusClass, "textStatus", "I");
		CHECK_EXCEPTION;
		if (!textStatusField) {
			NSLog(@"Warning: could not get text status field");
			return NO;
		}
	}
	
	static jfieldID pathField = nil;
	if (!pathField) {
		pathField = env->GetFieldID(statusClass, "path", "Ljava/lang/String;");
		CHECK_EXCEPTION;
		if (!pathField) {
			NSLog(@"Warning: could not get path field");
			return NO;
		}
	}
	
	NSMutableArray *changeList = [[NSMutableArray alloc] init];
	
	id changesController = [[NSApp delegate] valueForKey:@"changesController"];
	
	int i;
	for (i = 0; i < len; i++) {
		jobject item = env->GetObjectArrayElement(changesArray, i);
		CHECK_EXCEPTION;

		jint statusCode = env->GetIntField(item, textStatusField);
		CHECK_EXCEPTION;
		
		jstring jpath = (jstring) env->GetObjectField(item, pathField);
		CHECK_EXCEPTION;
		
		NSString *path = jstring_to_nsstring(env, jpath);
		CHECK_EXCEPTION;
		
		NSManagedObject *change = [changesController newObject];
		
		[change setValue:path forKey:@"absPath"];
		[change setValue:[path substringFromIndex:[localPath length] + 1] forKey:@"relPath"];
		[change setValue:[NSNumber numberWithBool:statusCode != 9] forKey:@"enabled"];
		[change setValue:[NSNumber numberWithInt:statusCode] forKey:@"status"];
				
		[changeList addObject:change];
		
		[change release];
	}
	
	if (changes)
		*changes = changeList;
	
	[changeList autorelease];
	
	return YES;
}

- (BOOL)commitItems:(NSArray *)items message:(NSString *)message returnError:(NSError **)error
{
	if (!helperRef)
		return NO;
	
	static jmethodID commitMethod = nil;
	if (!commitMethod) {
		commitMethod = env->GetMethodID(helperClass, "commit", "([Ljava/lang/String;)V");
		CHECK_EXCEPTION;
		if (!commitMethod) {
			NSLog(@"Warning: could not get commit method ID");
			return NO;
		}
	}
	
	static jclass stringClass = nil;
	if (!stringClass) {
		stringClass = env->FindClass("java/lang/String");
		CHECK_EXCEPTION;
		if (!stringClass) {
			NSLog(@"Failed to find java String class");
			return FALSE;
		}
	}
	
	int count = [items count];
	
    jobjectArray args = env->NewObjectArray(count, stringClass, NULL);
    CHECK_EXCEPTION;
    if (!args) {
        NSLog(@"could not create args array");
        return FALSE;
    }
	
	int i;
	for (i = 0; i < count; i++) {
		NSString *path = [items objectAtIndex:i];
		jstring jpath = nsstring_to_jstring(env, path);
		CHECK_EXCEPTION;
		
		env->SetObjectArrayElement(args, i, jpath);
		CHECK_EXCEPTION;
	}
	
//	jstring jcommit = nsstring_to_jstring(env, message);
	
	env->CallVoidMethod(helperRef, commitMethod, args);
	
	if (env->ExceptionCheck() == JNI_TRUE) {
		env->ExceptionDescribe();
		env->ExceptionClear();
		return NO;
	}
	
	return YES;
}

- (void)setLocalPath:(NSString *)path
{
	if (path == localPath)
		return;
	
	[localPath release];
	localPath = [path copy];
	
	if (helperRef) {
		[self attachCurrentThread];
		
		static jfieldID pathField = nil;
		if (!pathField) {
			pathField = env->GetFieldID(helperClass, "localPath", "Ljava/lang/String;");
			CHECK_EXCEPTION;
			if (!pathField) {
				NSLog(@"Warning: could not get localPath field");
				return;
			}
		}
		
		jstring jpath = nsstring_to_jstring(env, path);
		env->SetObjectField(helperRef, pathField, jpath);
		
		CHECK_EXCEPTION;
	}
}

- (BOOL)cancelReturnError:(NSError **)error
{
	if (!helperRef)
		return NO;
	
	[self attachCurrentThread];
	
	static jmethodID cancelMethod = nil;
	if (!cancelMethod) {
		cancelMethod = env->GetMethodID(helperClass, "cancelOperation", "()V");
		CHECK_EXCEPTION;
		if (!cancelMethod) {
			NSLog(@"Warning: could not get cancel method ID");
			return NO;
		}
	}
	
	env->CallVoidMethod(helperRef, cancelMethod);
	
	if (env->ExceptionCheck() == JNI_TRUE) {
		env->ExceptionDescribe();
		env->ExceptionClear();
		return NO;
	}
	
	return YES;
}

@end
