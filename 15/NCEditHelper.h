//
//  NCEditHelper.h
//  NCamera
//
//  Created by Kyungtaek, Lim on 12. 7. 25..
//  Copyright (c) 2012년 NTS. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NCAppImageProcessManager.h"
#import "NCEditHelperDelegate.h"
#import "NCImageItemInformation.h"

@interface NCEditHelper : NSObject <NCAppImageProcessDelegate>{
    
    NSInteger i_RetrayRequestCount;
    
}

@property (nonatomic, retain) NCAppImageProcessManager * processManager;
@property (nonatomic, retain) NCImageItemInformation   * filterInfoManager;
@property (nonatomic, assign) id<NCEditHelperDelegate>   delegate;
@property (nonatomic, retain) NSString                 * imageFileName;

-(void) requestImage:(NCEditRequestImageType)type;
-(void) requestFilterImageAtIndex:(NSInteger)index withType:(NCEditRequestImageType)type;
-(void) requestFilterImageAtKey:(NSString *)key withType:(NCEditRequestImageType)type;

-(NSInteger) saveImage:(UIImage *) image;
-(NSString *) checkFileName;

-(NSInteger) filterCount;
-(NSString *) filterKeyAtIndex:(NSUInteger) index;
-(NSString *) filterNameAtIndex:(NSUInteger) index;
-(NSInteger) filterIndexAtKey:(NSString *) filterName;
-(NSDictionary *) filterPropertyForKey:(NSString *) filterKey;

@end
