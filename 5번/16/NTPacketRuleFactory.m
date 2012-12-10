//
//  NTPacketRuleFactory.m
//  NTalk
//
//  Created by Jung TaeHoon on 11. 11. 30..
//  Copyright (c) 2011년 NHN Corporation. All rights reserved.
//

#import "NTPacketRuleFactory.h"
#import "NTPacketRule.h"
#import "NTPacketRuleSets.h"

#import "NetworkCommand.h"
#import "NTCommonDefine.h"
#import "NetworkCommand.h"
#import "NSString+Utils.h"


#pragma mark -
#pragma mark NTPacketRuleFactory (internal)

/**
 * @brief Private API
 */
@interface NTPacketRuleFactory (internal)

@end

/**
 * @brief Private API
 */
@implementation NTPacketRuleFactory (internal)

#define kPacketRulePrefix   @"NTPacketRuleSet_"

+ (BOOL)validateCommand:(NSUInteger)aCommand
{
    if (aCommand == 0) {
        return NO;
    }
    return YES;
}

+ (BOOL)isRequestCommand:(NSUInteger)aCommand
{
    int sResult = aCommand & NTC_RES_SRC_STARTCOMMAND;
    if (sResult > 0) {
        return NO;
    } else {
        return YES;
    }
}

+ (BOOL)isResponseCommand:(NSUInteger)aCommand
{
    return ![self isRequestCommand:aCommand];
}

+ (Class)classWithCommand:(NSUInteger)aCommand
{
    NSString *hexString = [NSString stringWithHexString:aCommand];
    NSString *clazzString = [NSString stringWithFormat:@"%@%@", kPacketRulePrefix, hexString];
    
    Class clazz = NSClassFromString(clazzString);
    
    return clazz;
}

+ (Class)classWithSuffixNameOfClass:(NSString *)aSuffixName
{
    NSString *clazzString = [NSString stringWithFormat:@"%@%@", kPacketRulePrefix, aSuffixName];
    
    Class clazz = NSClassFromString(clazzString);
    
    return clazz;
}

+ (NSMutableArray *)makeRuleSets:(NSUInteger)aCommand aParameter:(NSDictionary *)aParameter
{
    BOOL isResponseCommand = [self isResponseCommand:aCommand];
    
    NSUInteger requestCommand = aCommand;
    if (isResponseCommand) {
        requestCommand = aCommand ^ NTC_RES_SRC_STARTCOMMAND;
    }
    
//    DLog(@"Orginal: [%@] / Changed: [%@]", [NSString stringWithHexString:aCommand], [NSString stringWithHexString:requestCommand]);
    
    Class clazz = [self classWithCommand:requestCommand];
    
    if (!clazz) {
        ALog(@"\n\n\n Clazz is nil... command: [%@] / [%d], parameter: %@ \n\n\n", [NSString stringWithHexString:aCommand], aCommand, aParameter);
        NeloWarnLog(@"\n\n\n Clazz is nil... command: [%@] / [%d], parameter: %@ \n\n\n", [NSString stringWithHexString:aCommand], aCommand, aParameter);
        return nil;
    }
    
    SEL sel = @selector(requestPacketRuleSetWithParameter:);
    if (isResponseCommand) {
        sel = @selector(responsePacketRuleSetWithParameter:);
    }
    
    NTPacketBaseRuleSet *sRuleSet = [[[clazz alloc] init] autorelease];
    if (sRuleSet) {
        return [clazz performSelector:sel withObject:aParameter];
    } else {
        return nil;
    }
}

@end


#pragma mark -
#pragma mark NTPacketRuleFactory

/**
 * @brief Command 명을 이용하여, PacketRuleSet 객체를 가져오는 Factory
 * @author TaeHoon Jung.
 */
@implementation NTPacketRuleFactory

+ (NSMutableArray *)packetRuleSetWithCommand:(NSUInteger)aCommand
{
    return [self packetRuleSetWithCommand:aCommand parameter:nil];
}

+ (NSMutableArray *)packetRuleSetWithCommand:(NSUInteger)aCommand parameter:(NSDictionary *)aParameter
{
    if ([self validateCommand:aCommand] == NO) {
        return nil;
    }
    
    return [self makeRuleSets:aCommand aParameter:aParameter];
}

+ (NSMutableArray *)packetRuleSetWithSuffixNameOfClass:(NSString *)aSuffixName parameter:(NSDictionary *)aParameter
{
    Class clazz = [self classWithSuffixNameOfClass:aSuffixName];
    if (!clazz) {
        return nil;
    }
    
    SEL sel = @selector(requestPacketRuleSetWithParameter:);
    
    NTPacketBaseRuleSet *sRuleSet = [[[clazz alloc] init] autorelease];
    if (sRuleSet) {
        return [clazz performSelector:sel withObject:aParameter];
    } else {
        return nil;
    }
}

@end