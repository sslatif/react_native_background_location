//
//  VoiceChangerModule.m
//  sample
//
//  Created by Saqib on 14/07/2023.
//

#import <Foundation/Foundation.h>
#import "React/RCTBridgeModule.h"
@interface
RCT_EXTERN_MODULE(VoiceChangerModule, NSObject)
RCT_EXTERN_METHOD(changeVoiceToAlien)
RCT_EXTERN_METHOD(changeVoiceToChild)
RCT_EXTERN_METHOD(speedUpVoice)
RCT_EXTERN_METHOD(slowDownVoice)
@end
