//
//  DBManager.m
//  sample
//
//  Created by Saqib on 16/08/2023.
//

#import "DBManager.h"
#import "LocationData.h"
static DBManager *sharedInstance = nil;
static sqlite3 *database = nil;
static sqlite3_stmt *statement = nil;

@implementation DBManager

+(DBManager*)getSharedInstance {
  if (!sharedInstance) {
    sharedInstance = [[super allocWithZone:NULL]init];
    [sharedInstance createDB];
  }
  return sharedInstance;
}

-(BOOL)createDB {
  NSString *docsDir;
  NSArray *dirPaths;
  
  // Get the documents directory
  dirPaths = NSSearchPathForDirectoriesInDomains
  (NSDocumentDirectory, NSUserDomainMask, YES);
  docsDir = dirPaths[0];
  
  // Build the path to the database file
  databasePath = [[NSString alloc] initWithString:
                  [docsDir stringByAppendingPathComponent: @"MapData.db"]];
  BOOL isSuccess = YES;
  NSFileManager *filemgr = [NSFileManager defaultManager];
  
  if ([filemgr fileExistsAtPath: databasePath ] == NO) {
    const char *dbpath = [databasePath UTF8String];
    if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
      char *errMsg;
      const char *sql_stmt =
      "create table if not exists Location (_id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp text, latitude text, longitude text, altitude text, accuracy text, speed text)";
      
      if (sqlite3_exec(database, sql_stmt, NULL, NULL, &errMsg) != SQLITE_OK) {
        isSuccess = NO;
        NSLog(@"Failed to create table");
      }
      sqlite3_close(database);
      return  isSuccess;
    } else {
      isSuccess = NO;
      NSLog(@"Failed to open/create database");
    }
  }
  return isSuccess;
}

- (BOOL) saveData:(LocationData *)data {
  const char *dbpath = [databasePath UTF8String];
  
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    NSString *insertSQL = [NSString stringWithFormat:@"insert into Location (timestamp,latitude,longitude,altitude,accuracy,speed) values (\"%@\",\"%@\", \"%@\", \"%@\", \"%@\", \"%@\")",data.timestamp,
                           data.latitude, data.longitude, data.altitude,data.accuracy,data.speed];
    const char *insert_stmt = [insertSQL UTF8String];
    sqlite3_prepare_v2(database, insert_stmt,-1, &statement, NULL);
    
    if (sqlite3_step(statement) == SQLITE_DONE) {
      NSLog(@"Date saved!!.");
      return YES;
    } else {
      NSLog(@"unable to save Date!!!.");
      return NO;
    }
  }
  sqlite3_reset(statement);
  return NO;
}

- (NSArray<LocationData *>*) getAllRecords {
  NSMutableArray<LocationData *> *resultArray = [[NSMutableArray alloc] init];
  const char *dbpath = [databasePath UTF8String];
  
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    const char *query = "SELECT * FROM Location";
    sqlite3_stmt *statement;
    
    if (sqlite3_prepare_v2(database, query, -1, &statement, NULL) == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        LocationData *locationData = [[LocationData alloc] init];
        locationData.timestamp = [NSString stringWithUTF8String:(const char *)sqlite3_column_text(statement, 1)];
        locationData.latitude = [NSString stringWithUTF8String:(const char *)sqlite3_column_text(statement, 2)];
        locationData.longitude = [NSString stringWithUTF8String:(const char *)sqlite3_column_text(statement, 3)];
        locationData.altitude = [NSString stringWithUTF8String:(const char *)sqlite3_column_text(statement, 4)];
        locationData.accuracy = [NSString stringWithUTF8String:(const char *)sqlite3_column_text(statement, 5)];
        locationData.speed = [NSString stringWithUTF8String:(const char *)sqlite3_column_text(statement, 6)];
        
        [resultArray addObject:locationData];
      }
      
      // Finalize the statement to release resources
      sqlite3_finalize(statement);
    } else {
      NSLog(@"Error preparing statement: %s", sqlite3_errmsg(database));
    }
  } else {
    NSLog(@"Error opening database: %s", sqlite3_errmsg(database));
  }
  
  // Close the database connection
  sqlite3_close(database);
  
  return resultArray;
}


- (NSArray*) findByTimestamps:(NSString*)timestamp {
  const char *dbpath = [databasePath UTF8String];
  
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    NSString *querySQL = [NSString stringWithFormat:
                          @"select timestamp, latitude, longitude from Location where timestamp=\"%@\"",timestamp];
    const char *query_stmt = [querySQL UTF8String];
    NSMutableArray *resultArray = [[NSMutableArray alloc]init];
    
    if (sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL) == SQLITE_OK) {
      if (sqlite3_step(statement) == SQLITE_ROW) {
        NSString *timestamp = [[NSString alloc] initWithUTF8String:(const char *) sqlite3_column_text(statement, 1)];
        [resultArray addObject:timestamp];
        
        NSString *latitude = [[NSString alloc] initWithUTF8String:
                              (const char *) sqlite3_column_text(statement, 2)];
        [resultArray addObject:latitude];
        
        NSString *longitude = [[NSString alloc]initWithUTF8String:
                               (const char *) sqlite3_column_text(statement, 3)];
        [resultArray addObject:longitude];
        return resultArray;
      } else {
        NSLog(@"Not found");
        return nil;
      }
      
    }
  }
  sqlite3_reset(statement);
  return nil;
}

// Define a function to delete all records from a table in an SQLite database
- (void)deleteAllRecordsFromTable:(NSString *)tableName {
  const char *dbpath = [databasePath UTF8String];
  
  sqlite3 *database;
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    NSString *deleteQuery = [NSString stringWithFormat:@"DELETE FROM %@", tableName];
    const char *queryStatement = deleteQuery.UTF8String;
    
    if (sqlite3_exec(database, queryStatement, NULL, NULL, NULL) == SQLITE_OK) {
      NSLog(@"All records deleted from table '%@' successfully.", tableName);
    } else {
      NSLog(@"Error deleting records from table '%@': %s", tableName, sqlite3_errmsg(database));
    }
    
    sqlite3_close(database);
  } else {
    NSLog(@"Error opening database: %s", sqlite3_errmsg(database));
  }
}

@end
