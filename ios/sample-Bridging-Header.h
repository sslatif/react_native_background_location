//
//  Use this file to import your target's public headers that you would like to expose to Swift.
//

#import "React/RCTBridgeModule.h"
#import <React/RCTEventEmitter.h>
//@interface RNLocationChange : RCTEventEmitter <RCTBridgeModule>
@interface MyLocationDataManager : RCTEventEmitter <RCTBridgeModule>
@end
