//
//  NTPacket.m
//  NTalk
//
//  Created by Jung TaeHoon on 11. 11. 28..
//  Copyright (c) 2011년 NHN Corporation. All rights reserved.
//

#import "NTPacketRule.h"
#import "NTCommonDefine.h"

/**
 * @brief 소켓 데이터를 정의하며, 송/수신시에 사용된다.
 */
@implementation NTPacketRule

@synthesize type=mType, name=mName, data=mData, length=mLength, encrypt=mEncrypt;
@synthesize children=mChildren;

#pragma mark - class factory

+ (NTPacketRule *)packetWithName:(NSString *)aName data:(NSString *)aData length:(NSUInteger)aLength type:(NTPacketType)aType
{
    return [[[[self class] alloc] initWithName:aName data:aData length:aLength type:aType encrypt:NO] autorelease];
}

+ (NTPacketRule *)packetWithName:(NSString *)aName data:(NSString *)aData length:(NSUInteger)aLength type:(NTPacketType)aType encrypt:(BOOL)aEncrypt
{
    return [[[[self class] alloc] initWithName:aName data:aData length:aLength type:aType encrypt:aEncrypt] autorelease];
}


#pragma mark -

- (id)init
{
    self = [super init];
    if (self) {
        self.type = kNTPacketTypeInt;
        self.encrypt = NO;
        
        self.children = [NSMutableArray array];
    }
    return self;
}

- (id)initWithName:(NSString *)aName data:(NSString *)aData length:(NSUInteger)aLength type:(NTPacketType)aType
{
    self = [self init];
    if (self) {
        self.name = aName;
        self.data = aData;
        self.length = aLength;
        self.type = aType;
    }
    return self;
}

- (id)initWithName:(NSString *)aName data:(NSString *)aData length:(NSUInteger)aLength type:(NTPacketType)aType encrypt:(BOOL)aEncrypt
{
    self = [self initWithName:(NSString *)aName data:aData length:aLength type:aType];
    if (self) {
        self.encrypt = aEncrypt;
    }
    return self;
}

- (void)dealloc
{
    [mChildren release];
    [mName release];
    [mData release];
    
    [super dealloc];
}

- (NSString *)description
{
    return [NSString stringWithFormat:@"mName: %@, mData: %@, mLength: %d, mType: %@, mEncrypt: %@, children: %@", 
            mName, mData, mLength, mType==kNTPacketTypeInt?@"kNTPacketTypeInt":@"kNTPacketTypeString", mEncrypt?@"YES":@"NO", mChildren];
}

#pragma mark - NSCoding

- (id)copyWithZone:(NSZone *)zone
{
    NTPacketRule *sCopy = [[[self class] allocWithZone:zone] init];
    
    sCopy.name = mName;
    sCopy.data = mData;
    sCopy.length = mLength;
    sCopy.type = mType;
    sCopy.encrypt = mEncrypt;
    sCopy.children = mChildren;
        
    return sCopy;
}

#pragma mark - setter / getter

- (void)setData:(id)data
{
    [mData release];
    
    if ([data isKindOfClass:[NSString class]]) 
    {
        mData = [data retain];
    } 
    else if ([data isKindOfClass:[NSNumber class]]) 
    {
        mData = [[NSString stringWithFormat:@"%d", [data intValue]] retain];
    } 
    else if ([data isKindOfClass:[NSData class]]) 
    {
        mData = [data retain];
    } 
    else 
    {
        mData = [data retain];
    }
}

#pragma mark -
#pragma mark parse...

- (const void *)bytes
{
    const void * sBytes = NULL;
    switch (mType) 
    {
        case kNTPacketTypeInt:
        {
            NSString *mLowerStringData = [mData lowercaseString];
            if ([mLowerStringData hasPrefix:@"0x"]) //ex> 0x6c00 ...
            {
                NSScanner *sScanner = [NSScanner scannerWithString:mLowerStringData];
                [sScanner scanHexInt:&mIntValue];    
            } 
            else //ex> 16 number string
            {
                mIntValue = [mData intValue];
            }
            
            sBytes = &mIntValue;
        }
            break;
            
        case kNTPacketTypeString:
        case kNTPacketTypeJson:
        {
            if ([mData isEqualToString:NULL_PACKET]) 
            {
                NSScanner *sScanner = [NSScanner scannerWithString:mData];
                [sScanner scanHexInt:&mIntValue];
                
                return &mIntValue;
            } 
            else 
            {
//                const char* sResultValue = [mData UTF8String];    
                const char* sResultValue = [mData cStringUsingEncoding:NSUTF8StringEncoding];    
                
//                NSString *ss = [[NSString alloc] initWithBytes:sResultValue length:mLength encoding:NSUTF8StringEncoding];
//                NSString *ss = [NSString stringWithUTF8String:sResultValue];
//                DLog(@"%@ / %@", mData, ss);
                
                sBytes = sResultValue;
            }
        }
            break;
            
        case kNTPacketTypeData:
            sBytes = [mData bytes];
            break;
            
        default:
            break;
    }
    
    return sBytes;
}




#pragma mark -
#pragma mark 

- (NSUInteger)lengthWithChildren
{
    NSUInteger sTotalLength = mLength;
    
    if (mChildren) {
        for (NTPacketRule *sChild in mChildren) {
            sTotalLength += sChild.lengthWithChildren;
        }
    }
    
    return sTotalLength;
}

- (BOOL)haveChildren
{
    if (mChildren) {
        if ([mChildren count] > 0) {
            return YES;
        } else {
            return NO;
        }
    }
    return NO;
}

@end
