/**
 @file NCTransManager.h
 @date 11. 5. 12.
 @author Kyungtaek, Lim / NHN technology services Co.,Ltd. Copyright reserved.
 @brief 
 @remarks
 @warning
 @see 
 */

#import <Foundation/Foundation.h>
#import "NCTransDefines.h"
#import "NCTransDelegate.h"
#import "NCUploadManager.h"
#import "NCTransInfoModel.h"
#import "NCAppPropertyInfo.h"
#import "NCAutobackupManager.h"

@interface NCAppTransManager : NSObject <NCTransDelegate> {

    NCUploadManager     * i_UploadManager;              ///< 업로드 매니저    
    NCTransInfoModel    * i_CurrentExecuteInfo;         ///< 현재 전송중이 항목
}

@property (assign)              NCTransInfoModel*   currentExecuteInfo;

+(NCAppTransManager *) getSharedInstance;

-(BOOL) isUploading;
-(void) executeUpload:(NCTransInfoModel *)info;
-(void) cancelUpload;

-(NCNetworkState) isNetworkState;

@end
