//
//  DBManager.h
//  sample
//
//  Created by Saqib on 16/08/2023.
//

#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import "LocationData.h"

@interface DBManager : NSObject {
   NSString *databasePath;
}

+(DBManager*)getSharedInstance;
-(BOOL)createDB;
-(BOOL) saveData:(LocationData *)locationData;
-(NSArray*) findByTimestamps:(NSString*)timestamp;
-(NSArray<LocationData *>*) getAllRecords;
- (void)deleteAllRecordsFromTable:(NSString *)tableName;

@end
