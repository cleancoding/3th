//
//  NCBaseModel.m
//  NaverCafe
//
//  Created by 김원겸 on 12. 10. 4..
//  Copyright (c) 2012년 NHN. All rights reserved.
//

#import "NCBaseModel.h"
#import <objc/runtime.h>
#import "SBJson.h"
#import "MasterHeaders.h"

@implementation NCBaseModel

- (id)initWithDictionary:(NSDictionary *)dictionary {
    self = [super init];
    
    if (self) {
        NSArray *properties = [self propertyNames];
        for (NSString *property in properties) {
            [self setValue:[dictionary valueForKey:property] forKey:property];
        }
    }
    
    return self;
}

- (NSArray *)propertyNames {
    unsigned int propertyCount = 0;
    objc_property_t *properties = class_copyPropertyList([self class], &propertyCount);
    NSMutableArray *results = [NSMutableArray arrayWithCapacity:propertyCount];
    
    for (int i = 0; i < propertyCount; i++) {
        objc_property_t property = properties[i];
        const char *propertyName = property_getName(property);
        [results addObject:[NSString stringWithUTF8String:propertyName]];
    }
    
    free(properties);
    
    return results;
}

- (NSDictionary *)toDictionary {
    NSArray *propertyNames = [self propertyNames];
    
    NSMutableDictionary *results = [[NSMutableDictionary alloc] initWithCapacity:[propertyNames count]];
    for (NSString *propertyName in propertyNames) {
        id value = [self valueForKey:propertyName];
        
        if (value == nil) {
            value = [NSNull null];
        }
        
        [results setObject:value forKey:propertyName];
    }
    
    return results;
}

- (NSDictionary *)_toDictionary {
    NSDictionary *properties = [self toDictionary];
    NSMutableDictionary *results = [NSMutableDictionary dictionaryWithCapacity:[properties count]];
    
    for (NSString *key in [properties keyEnumerator]) {
        id value = [properties valueForKey:key];
        
        if ([[value class] isSubclassOfClass:[NCBaseModel class]]) {
            value = [value _toDictionary];
        }
        
        [results setValue:value forKey:key];
    }
    
    return results;
}

- (NSString *)description {
    return [[self _toDictionary] JSONPrettyRepresentation];
}

@end
