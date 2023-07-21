#import "sample-Bridging-Header.h"
#import <CoreLocation/CLError.h>
#import <CoreLocation/CLLocationManager.h>
#import <CoreLocation/CLLocationManagerDelegate.h>


@implementation MyLocationDataManager
{
 CLLocationManager * locationManager;
 NSDictionary * lastLocationEvent;
}
- (dispatch_queue_t)methodQueue
{
 return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE(MyLocationDataManager);
- (NSDictionary *)constantsToExport
{
 return @{ @"listOfPermissions": @[@"significantLocationChange"] };
}
+ (BOOL)requiresMainQueueSetup
{
 return YES;  // only do this if your module exports constants or calls UIKit
}
//all methods currently async
RCT_EXPORT_METHOD(initialize:(RCTPromiseResolveBlock)resolve
                rejecter:(RCTPromiseRejectBlock)reject) {
 //RCTLogInfo(@"Pretending to do something natively: initialize");
 resolve(@(true));
}
RCT_EXPORT_METHOD(hasPermissions:(NSString *)permissionType
                hasPermissionsWithResolver:(RCTPromiseResolveBlock)resolve
                rejecter:(RCTPromiseRejectBlock)reject) {
 //RCTLogInfo(@"Pretending to do something natively: hasPermissions %@", permissionType);

 BOOL locationAllowed = [CLLocationManager locationServicesEnabled];

 resolve(@(locationAllowed));
}
RCT_EXPORT_METHOD(requestPermissions:(NSString *)permissionType
                requestPermissionsWithResolver:(RCTPromiseResolveBlock)resolve
                rejecter:(RCTPromiseRejectBlock)reject)
{
 NSArray *arbitraryReturnVal = @[@"testing..."];
 //RCTLogInfo(@"Pretending to do something natively: requestPermissions %@", permissionType);

 // location
 if (!locationManager) {
   //RCTLogInfo(@"init locationManager...");
   locationManager = [[CLLocationManager alloc] init];
 }

 locationManager.delegate = self;
 locationManager.allowsBackgroundLocationUpdates = true;
 locationManager.pausesLocationUpdatesAutomatically = true;
 if ([locationManager respondsToSelector:@selector(requestAlwaysAuthorization)]) {
   [locationManager requestAlwaysAuthorization];
 } else if ([locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
   [locationManager requestWhenInUseAuthorization];
 }
 [locationManager startUpdatingLocation];
 [locationManager startMonitoringSignificantLocationChanges];
 resolve(arbitraryReturnVal);
}
- (NSArray *)supportedEvents {
   return @[@"significantLocationChange"];
}
- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
   CLLocation* location = [locations lastObject];

   lastLocationEvent = @{
                         @"coords": @{
                                 @"latitude": @(location.coordinate.latitude),
                                 @"longitude": @(location.coordinate.longitude),
                                 @"altitude": @(location.altitude),
                                 @"accuracy": @(location.horizontalAccuracy),
                                 @"altitudeAccuracy": @(location.verticalAccuracy),
                                 @"heading": @(location.course),
                                 @"speed": @(location.speed),
                                 },
                         @"timestamp": @([location.timestamp timeIntervalSince1970] * 1000) // in ms
                       };
   //RCTLogInfo(@"significantLocationChange : %@", lastLocationEvent);

   // TODO: do something meaningful with our location event. We can do that here, or emit back to React Native
   // https://facebook.github.io/react-native/docs/native-modules-ios.html#sending-events-to-javascript
}
@end
