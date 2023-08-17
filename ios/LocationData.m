//
//  LocationData.m
//  sample
//
//  Created by Saqib on 17/08/2023.
//

#import "LocationData.h"

@implementation LocationData

- (NSDictionary *)toDictionary {
    return @{
        @"timestamp": self.timestamp ?: [NSNull null],
        @"latitude": self.latitude ?: [NSNull null],
        @"longitude": self.longitude ?: [NSNull null],
        @"altitude": self.altitude ?: [NSNull null],
        @"accuracy": self.accuracy ?: [NSNull null],
        @"speed": self.speed ?: [NSNull null]
    };
}

@end

