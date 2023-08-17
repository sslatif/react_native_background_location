//
//  LocationData.h
//  sample
//
//  Created by Saqib on 17/08/2023.
//

#import <Foundation/Foundation.h>

@interface LocationData : NSObject

@property (nonatomic, strong) NSString *timestamp;
@property (nonatomic, strong) NSString *latitude;
@property (nonatomic, strong) NSString *longitude;
@property (nonatomic, strong) NSString *altitude;
@property (nonatomic, strong) NSString *accuracy;
@property (nonatomic, strong) NSString *speed;

@end
