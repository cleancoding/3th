//
//  NCEditHelper.m
//  NCamera
//
//  Created by Kyungtaek, Lim on 12. 7. 25..
//  Copyright (c) 2012년 NTS. All rights reserved.
//

#import "NCEditHelper.h"
#import "NCAppImageFileManager.h"
#import "NCAppCreator.h"

@implementation NCEditHelper

@synthesize processManager = _processManager;
@synthesize delegate = _callbackDelegate;
@synthesize imageFileName = _imageFileName;
@synthesize filterInfoManager = _filterInfoManager;

-(id) init {
    
    self = [super init];
    
    if(self != nil){
        
        NCAppImageProcessManager * processManager = [[NCAppImageProcessManager alloc]initWithDelegate:self];
        
        self.processManager = processManager;
        
        [processManager release];
        
        NCImageItemInformation * itemInformation = [[[NCAppCreator createClass:NC_STR_FILTER_PLIST_FILE_NAME
                                                                 withClassForm:NC_STR_IMAGE_PLIST_FILE_FORM] alloc]init];
        
        self.filterInfoManager = itemInformation;
        
        [itemInformation release];
        
        i_RetrayRequestCount = 0;
    }
    
    return self;
}

- (void) dealloc {
    
    [self.processManager clearImageForCacheData];
    
    self.imageFileName = nil;
    self.filterInfoManager = nil;
    self.processManager = nil;
    self.delegate = nil;
    
    [super dealloc];
}

-(NSInteger) saveImage:(UIImage *) image {
    
    NSString * fileName = [self checkFileName];
    
    BOOL result = [[NCAppImageFileManager getSharedInstance] save:image withFileName:fileName andDataType:NC_IMAGE_DATA_TYPE_ORIGIN];
    
    if(result == YES){
        [self.processManager readImageInfoFromFileManager];
        return [self.processManager imageIndexForFileName:fileName];
    }
    
    return -1;
}

-(NSString *) checkFileName {
    
    NSString * fileName =  [self.imageFileName stringByDeletingPathExtension];
    
    NSString * resultFileName = [NSString stringWithFormat:@"%@_Edit.jpg",fileName];
    
    BOOL isExist = [[NCAppImageFileManager getSharedInstance] isExistFileName:resultFileName withDataType:NC_IMAGE_DATA_TYPE_ORIGIN];
    
    NSInteger count = 1;
    
    while(isExist == YES) {
        
        resultFileName = [NSString stringWithFormat:@"%@_Edit(%d).jpg",fileName, count];
        
        isExist = [[NCAppImageFileManager getSharedInstance] isExistFileName:resultFileName withDataType:NC_IMAGE_DATA_TYPE_ORIGIN];
        
        count++;
    }
    
    return resultFileName;
}

-(void) requestImage:(NCEditRequestImageType)type {
    
    [self requestFilterImageAtIndex:-1 withType:type];
}

-(void) requestFilterImageAtIndex:(NSInteger)index withType:(NCEditRequestImageType)type {
    
    NSString * filterKey = nil;
    
    if(index >= 0){
        filterKey = [self filterKeyAtIndex:index];
    }
    
    [self requestFilterImageAtKey:filterKey withType:type];
}

-(void) requestFilterImageAtKey:(NSString *)key withType:(NCEditRequestImageType)type {
    
    i_RetrayRequestCount = 0;
    
    NCImageEffectInfo * imageEffectInfo = [[NCImageEffectInfo alloc] initWIthImageName:self.imageFileName
                                                                             filterKey:key
                                                                              dataType:type
                                                                   autoCorrectionState:NO
                                                                               callTag:type];
    
    [self.processManager performSelector:@selector(showImage:) withObject:imageEffectInfo afterDelay:0.1];
    
    [imageEffectInfo release];
}

-(void) completeImage:(NCImageEffectInfo *)info {
    
    if(self.delegate == nil){
        return;
    }
    
    UIImage * resultImage = [self.processManager outputImage:info.resultFileName withDataType:info.commandType];
    
    if( resultImage == nil){
        
        [self.processManager performSelector:@selector(showImage:) withObject:info afterDelay:0.1];
        
        return;
    }
    
    [self.delegate displayImage:resultImage atType:info.commandType withFilterKey:info.filterKey];
    
    i_RetrayRequestCount = 0;
}

-(void) failImage:(NCImageEffectInfo *)info {
    
    if(i_RetrayRequestCount < 3){
        [self.processManager performSelector:@selector(showImage:) withObject:info afterDelay:0.1];
        i_RetrayRequestCount++;
        return;
    }
    
    SALog2(@"이미지 생성 실패 : %@ | [%d]", info.resultFileName, info.commandType);
}

#pragma mark - Filter Infomation Method

-(NSInteger) filterCount {
    
    return [self.filterInfoManager itemCount];
}

-(NSString *) filterKeyAtIndex:(NSUInteger) index {
    
    return [self.filterInfoManager itemKeyAtIndex:index];
}

-(NSString *) filterNameAtIndex:(NSUInteger) index {
    
    return [self.filterInfoManager itemNameAtIndex:index];
}


-(NSInteger) filterIndexAtKey:(NSString *) filterKey {
    
    if(filterKey == nil){
        return -1;
    }
    
    return [self.filterInfoManager itemIndexForKey:filterKey];
}

-(NSDictionary *) filterPropertyForKey:(NSString *) filterKey {
    
    return [self.filterInfoManager itemPropertyDictionaryForKey:filterKey];
}


@end
