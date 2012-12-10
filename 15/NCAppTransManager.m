/**
 @file NCTransManager.m
 @date 11. 5. 12.
 @author Kyungtaek, Lim / NHN technology services Co.,Ltd. Copyright reserved.
 @brief 뷰에서 요청하는 전송에 대한 모든 관리를 담당하는 싱글톤 클래스.
 @remarks 이 클래스에서 전송에 대한 정보를 모두 관리하므로로 NCUploadManager가 호출되면 안됨 클래스를 호출하면 안됨
 @warning
 @see 
 */

#import "NCAppTransManager.h"
#import "NCAppImageFileManager.h"
#import "NCAppPropertyInfo.h"
#import "NCAppPropertyManager.h"
#import "NCAppLoginManager.h"
#import "NCLoginDefines.h"
#import "NSMutableArray+QueueAddtions.h"

#pragma mark - Anonymous category
@interface NCAppTransManager () 

///< internal properties

@property (nonatomic, retain)   NCUploadManager *   uploadManager;

-(void) postTransResultNotification:(NCTransInfoModel *)info 
                         withResult:(NCTransResult)result;

-(void) postTransUpdateStateNotification:(NCTransInfoModel *)info 
                         withUpdateState:(NCTransUpdateState)updateState 
                         withProgressive:(float)progressValue;

@end

#pragma mark - Class Implementations
/**
 @brief 전송에 대한 전반적인 동작을 수행하는 매니저 클래스
 */
@implementation NCAppTransManager

#pragma mark - Synthesize
@synthesize uploadManager           = i_UploadManager;
@synthesize currentExecuteInfo      = i_CurrentExecuteInfo;

SYNTHESIZE_SINGLETON_CLASS(NCAppTransManager, getSharedInstance);

#pragma mark - NSObject default methods
-(id) init {
    self = [super init];
    
    if(self != nil) {
    
        ///< 업로드 매니저 초기화
        NCUploadManager *tUploadManager = [[NCUploadManager alloc] init];
        self.uploadManager = tUploadManager;
        [tUploadManager release];
                
        ///< 현재 전송중인 항목 가리키는 변수 초기화
        self.currentExecuteInfo = nil;
    }
    
    return self;
}

-(void) dealloc {

    ///< 객체들을 메모리에서 해제.
    self.uploadManager      = nil;
    self.currentExecuteInfo = nil;
    
    [super dealloc];
}

#pragma mark - configuration methods
/**
 *  @brief: 해당 서비스 타입에 대한 로그인 여부를 체크한다.
 */
-(BOOL) checkLoginType:(NCServiceCode)serviceCode
{
    NCAppLoginManager *service = [NCAppLoginManager getSharedInstance];
    return [service isLoginServiceCode:serviceCode];
}

/**
 *  @brief: 현재 네트워크 종류를 리턴한다.
 */
-(NCNetworkState) isNetworkState
{
    return [i_UploadManager isNetworkState];
}

-(BOOL) isUploading
{
    BOOL ret = NO;
    
    if ( self.currentExecuteInfo != nil )   
    {
        ret = YES;
    }
    if ( self.uploadManager.directOperationCount > 0 || self.uploadManager.autoBackupOperationCount > 0 )
    {
        ret = YES;
    }
    
    return ret;
}

#pragma mark - Managing trans Method
/**
 @breif 전송을 시도한다.
 */
-(void) executeUpload:(NCTransInfoModel *)info {

    SALog2(@"** Upload TransInfo model:\n%@", info);

    // 해당 서비스에 로그인이 되어있지 않다면, 자동전송을 중지하고 수행하지 않는다.
    if ( ![self checkLoginType:info.serviceCode] )
    {
        SALog2(@"** FAIL: Not Login : %d", info.serviceCode);      
        
        return;
    }

    // 네트워크가 연결되어있지 않을 경우, 전송을 시도하지 않고 실패로 간주한다.
    if([self isNetworkState] == NETWORK_STATE_NOT){
        
        SALog2(@"** Fail: Network is not available.");
        info.errorCode = NC_ERROR_NETWORK_NOT_CONNECT;
        [self fail:info];
        
        return;
    }

    // 전송을 시작하기 전에 멤버변수를 초기화한다.
    self.currentExecuteInfo = info;


    // 전송 종류에 따라 전송을 시작한다.
    switch (info.transType) { 
        case NC_TRANS_TYPE_DIRECT:
        {            
            if ( self.currentExecuteInfo.filePath == nil )
            {
                self.currentExecuteInfo.filePath = [[NCAppImageFileManager getSharedInstance] 
                                                    pathWithFileName:info.autoBackupFileName 
                                                    andDataType:NC_IMAGE_DATA_TYPE_ORIGIN];
            }
            
            [self.uploadManager doDirectUpload:self.currentExecuteInfo withCallBack:self];
            
            break;   
        }
        case NC_TRANS_TYPE_QUEUE:
        {
            if ( self.currentExecuteInfo.filePath == nil )
            {
                self.currentExecuteInfo.filePath = [[NCAppImageFileManager getSharedInstance] 
                                                    pathWithFileName:info.autoBackupFileName 
                                                    andDataType:NC_IMAGE_DATA_TYPE_ORIGIN];
            }
            
            [self.uploadManager doUploadInfo:self.currentExecuteInfo withCallBack:self];
                        
            break;
        }
        default:
        {
            break;
        }
    }
    
    // 전송이 시작되었음을 알리는 노티피케이션을 보낸다.
    [self postTransUpdateStateNotification:info 
                           withUpdateState:NC_TRANS_UPDATESTATE_START 
                           withProgressive:0.0f];

}

-(void) cancelUpload
{
    [self.uploadManager cancelCurrentUpload];
}

#pragma mark - NCTransDelegate Protocol
/**
 @brief 전송이 성공했을 때 호출되는 delegate 함수이다.
 */
-(void) complete:(NCTransInfoModel *)info {

    @synchronized(self){

        SALog2(@"** Complete transInfo: \n%@", info);
        
        // 전송이 완료된 후 멤버변수를 초기화한다.
        self.uploadManager.executeAutoBacupTransId = nil;
        self.currentExecuteInfo = nil;

        info.failCount = 0;
        
        // 전송이 성공했음을 노티피케이션으로 알린다.
        [self postTransResultNotification:info withResult:NC_TRANS_RESULT_COMPLETE];
    }
}

/**
 @brief 전송이 실패했을 때 호출되는 delegate 함수이다.
 */
-(void) fail:(NCTransInfoModel *)info {
    
    @synchronized(self)
    {
        // 재전송 시도횟수가 남아있다면 재전송을 시도한다.
        self.uploadManager.executeAutoBacupTransId = nil;
        
        if ([self.currentExecuteInfo isEqual:info] == YES)
        {
            self.currentExecuteInfo = nil;
        }
    
        info.failCount++;
        
        if(info.failCount < 0 ) 
        {
            SALog2(@"** Fail to Send Fail (%d). Auto ReTry! : ERROR code:%d",info.failCount, info.errorCode);
            [self performSelector:@selector(executeUpload:) withObject:info afterDelay:2.0f];
            return;
        }
        
        // 재전송 시도를 초과하였을 경우, 실패로 처리한다.
        SALog2(@"** Trans Fail. Stop Trans ERROR code:%d", info.errorCode);
                
        [self postTransResultNotification:info withResult:NC_TRANS_RESULT_FAIL];        
    }
}

/**
 @brief 전송을 취소했을 때 호출되는 delegate 함수이다.
 */
-(void) cancel:(NCTransInfoModel *)info
{
    @synchronized(self)
    {
        ///< 전송실패 후 후처리를 수행한다.
        self.currentExecuteInfo = nil;
        info.failCount = 0;
        
        ///< 전송이 실패했음을 Notification으로 알린다.
        [self postTransResultNotification:info withResult:NC_TRANS_RESULT_CANCEL];
    }
}

-(void) currentUploadProgress:(double)progress
{
    SALog2(@"** Uploading: %f ...", progress);
    
    [self postTransUpdateStateNotification:i_CurrentExecuteInfo
                           withUpdateState:NC_TRANS_UPDATESTATE_ING
                           withProgressive:progress];
}


#pragma mark - NSNotification methods
/**
 *  @brief: 파일 전송 결과를 알려준다.
 */
-(void) postTransResultNotification:(NCTransInfoModel *)info withResult:(NCTransResult)result
{    
    if ( info == nil ) 
    {
        SALog2(@"** Error: TransInfo is nil");
        return;
    }

    NSMutableDictionary *userDict = [NSMutableDictionary dictionary];
    
    [userDict setObject:info forKey:NC_NOTIKEY_TRANSINFO];
    [userDict setObject:[NSNumber numberWithInt:result] forKey:NC_NOTIKEY_TRANS_RESULT_VALUE];
    
    NSNotificationCenter *ns = [NSNotificationCenter defaultCenter];
    if ( ns != nil ) 
    {
        [ns postNotificationName:NC_NOTI_TRANS_RESULT object:self userInfo:userDict];
    }
}


/**
 *  @brief: 네트워크를 통한 전송 상태를 알려준다.
 */
-(void) postTransUpdateStateNotification:(NCTransInfoModel *)info withUpdateState:(NCTransUpdateState)updateState withProgressive:(float)progressValue
{
    if ( info == nil ) 
    {
        SALog2(@"** Error: TransInfo is nil");
        return;
    }
    
    NSMutableDictionary *userDict = [NSMutableDictionary dictionary];
    info.transUpdateState = updateState;
    info.transProgress = progressValue;
    [userDict setObject:info forKey:NC_NOTIKEY_TRANSINFO];
    
    NSNotificationCenter *ns = [NSNotificationCenter defaultCenter];
    if ( ns != nil ) 
    {
        [ns postNotificationName:NC_NOTI_TRANS_UPDATE_STATE object:self userInfo:userDict];
    }
}

@end
