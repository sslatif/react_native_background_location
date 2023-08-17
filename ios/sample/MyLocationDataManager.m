#import "sample-Bridging-Header.h"
#import <CoreLocation/CLError.h>
#import <CoreLocation/CLLocationManager.h>
#import <CoreLocation/CLLocationManagerDelegate.h>
#import "DBManager.h"
#import "LocationData.h"

@implementation MyLocationDataManager
{
  // Declare latitude and longitude variables
  double lastLatitude;
  double lastLongitude;
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
  lastLatitude =0.0;
  lastLongitude=0.0;
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
RCT_EXPORT_METHOD(getAllDatabaseRecords:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    NSArray<LocationData *> *records = [[DBManager getSharedInstance] getAllRecords];
    NSMutableArray *serializedRecords = [NSMutableArray array];
    
    if (records) {
        for (LocationData *record in records) {
            NSDictionary *recordDict = @{
                @"timestamp": record.timestamp ?: [NSNull null],
                @"latitude": record.latitude ?: [NSNull null],
                @"longitude": record.longitude ?: [NSNull null],
                @"altitude": record.altitude ?: [NSNull null],
                @"accuracy": record.accuracy ?: [NSNull null],
                @"speed": record.speed ?: [NSNull null]
            };
            [serializedRecords addObject:recordDict];
        }
        resolve(serializedRecords);
    } else {
        NSString *errorDomain = @"com.myapp.errors";
        NSString *errorMessage = @"Failed to retrieve database records.";
        NSDictionary *errorInfo = @{ NSLocalizedDescriptionKey: errorMessage };
        NSError *error = [NSError errorWithDomain:errorDomain code:-1 userInfo:errorInfo];
        reject(@"db_error", @"Failed to retrieve database records.", error);
    }
}

RCT_EXPORT_METHOD(deleteAllRecordsFromTable:(NSString *)tableName) {
  [[DBManager getSharedInstance] deleteAllRecordsFromTable:@"Location"];
}

- (NSString *)formatTimestamp:(CLLocation *)location {
  NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
  [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
  
  NSString *formattedDate = [dateFormatter stringFromDate:location.timestamp];
  
  NSLog(@"Formatted Date: %@", formattedDate);
  return formattedDate;
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
  CLLocation* location = [locations lastObject];
  
  lastLocationEvent = @{
    @"coords": @{
      @"timestamp": @([location.timestamp timeIntervalSince1970] * 1000),
      @"latitude": @(location.coordinate.latitude),
      @"longitude": @(location.coordinate.longitude),
      @"altitude": @(location.altitude),
      @"accuracy": @(location.horizontalAccuracy),
      @"altitudeAccuracy": @(location.verticalAccuracy),
      @"heading": @(location.course),
      @"speed": @(location.speed),
    },
    //@"timestamp": @([location.timestamp timeIntervalSince1970] * 1000) // in ms
  };
  
  double currentLatitude =[@(location.coordinate.latitude) doubleValue];
  double currentLongitude =[@(location.coordinate.longitude) doubleValue];
  double distance = haversineDistance(lastLatitude,lastLongitude,currentLatitude,currentLongitude);
  if(distance>5){
    lastLatitude = currentLatitude;
    lastLongitude = currentLongitude;
    
    NSString *formattedDate = [self formatTimestamp:location];
    NSString *latitude = [NSString stringWithFormat:@"%.4f", location.coordinate.latitude];
    NSString *longitude = [NSString stringWithFormat:@"%.4f", location.coordinate.longitude];
    NSString *altitude = [NSString stringWithFormat:@"%@", @(location.altitude)];
    NSString *speed = [NSString stringWithFormat:@"%@", @(location.speed)];
    NSString *accuracy = [NSString stringWithFormat:@"%@", @(location.horizontalAccuracy)];
    if(formattedDate != nil && formattedDate.length>1){
      LocationData *locationData = [[LocationData alloc] init];
      locationData.timestamp = formattedDate;
      locationData.latitude = latitude;
      locationData.longitude = longitude;
      locationData.altitude = altitude;
      locationData.accuracy = speed;
      locationData.speed = accuracy;
      [[DBManager getSharedInstance] saveData:locationData];
      NSLog(@"Distance between points: %.2f meters", distance);
    } else {
      NSLog(@"Formatted Date is null.");
    }
  }
  
  [self sendEventWithName:@"significantLocationChange" body:lastLocationEvent];
  //currentLocation = [[CLLocation alloc] init];
  //NSLog(@"significantLocationChange : %@", lastLocationEvent);
  
  // TODO: do something meaningful with our location event. We can do that here, or emit back to React Native
  // https://facebook.github.io/react-native/docs/native-modules-ios.html#sending-events-to-javascript
}

double degreesToRadians(double degrees) {
  return degrees * M_PI / 180.0;
}

double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
  double earthRadius = 6371000; // Earth's radius in meters
  
  double dLat = degreesToRadians(lat2 - lat1);
  double dLon = degreesToRadians(lon2 - lon1);
  
  double a = sin(dLat / 2) * sin(dLat / 2) + cos(degreesToRadians(lat1)) * cos(degreesToRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2);
  double c = 2 * atan2(sqrt(a), sqrt(1 - a));
  
  return earthRadius * c;
}

@end
