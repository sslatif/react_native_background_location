import React, { useState, useEffect } from 'react'
// Import required components
import {
  SafeAreaView,
  StyleSheet,
  View,
  Text,
  Platform,
  Alert,
  PermissionsAndroid,
  DeviceEventEmitter,
  Button,
} from 'react-native'
import MapView, { Marker } from 'react-native-maps'
import { PROVIDER_GOOGLE } from 'react-native-maps' // remove PROVIDER_GOOGLE import if not using Google Maps
import Realm from 'realm'
import Geolocation from '@react-native-community/geolocation'
import { NativeModules, NativeEventEmitter } from 'react-native'
const { LocationServiceModule } = NativeModules
var location = NativeModules.MyLocationDataManager
const eventEmitter = new NativeEventEmitter(location)

async function requestPermissions() {
  const result = await location.requestPermissions('')
  return result
}

// //request the permission before starting the service.
async function requestBackgroundPermission() {
  try {
    const backgroundgranted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
      {
        title: 'Background Location Permission',
        message:
          'We need access to your location ' +
          'so you can get live quality updates.',
        buttonNeutral: 'Ask Me Later',
        buttonNegative: 'Cancel',
        buttonPositive: 'OK',
      },
    )

    if (backgroundgranted === PermissionsAndroid.RESULTS.GRANTED) {
      console.log('Background location permission granted')
    } else {
      console.log('Background location permission denied')
    }
  } catch (error) {
    console.error(
      'Error occurred while requesting background location permission:',
      error,
    )
  }
}

// schema for database objects
const TaskSchema = {
  name: 'MapData',
  properties: {
    lat: 'string',
    long: 'string',
    alt: 'string',
    direct: 'string',
    accuracy: 'string',
    spd: 'string',
    timestamp: 'string',
    _id: 'int',
  },
  primaryKey: '_id',
}

const App = () => {
  const [currentLatitude, setCurrentLatitude] = useState(0.0)
  const [currentLongitude, setCurrentLongitude] = useState(0.0)
  const [altitude, setAltitude] = useState(0.0)
  const [accuracy, setAccuracy] = useState(0.0)
  const [speed, setSpeed] = useState(0.0)
  const [timeStamp, setTimeStamp] = useState(0)

  const [direction, setDirection] = useState('')
  const [state, setState] = useState({ events: [] })
  const [newLoc, setNewLoc] = useState('0.0')

  var lastLatitude = 0.0
  var lastLongitude = 0.0

  const [region, setRegion] = useState({
    latitude: 51.5079145,
    longitude: -0.0899163,
    latitudeDelta: 0.01,
    longitudeDelta: 0.01,
  })

  const startService = () => {
    Platform.OS === 'android' && LocationServiceModule.startLocationService()

    if (Platform.OS === 'android') {
      DeviceEventEmitter.addListener('location', function (e: Event) {
        console.log('DeviceEventEmitter Location Listener', e)
        setCurrentLatitude(parseFloat(e.latitude))
        setCurrentLongitude(parseFloat(e.longitude))
        setAltitude(parseFloat(e.altitude))
        setAccuracy(parseFloat(e.accuracy))
        setSpeed(parseFloat(e.speed))
        setTimeStamp(parseInt(e.timestamp))

        addressToShow =
          'LatLng:' +
          e.latitude +
          ',' +
          e.longitude +
          ',Alt:' +
          e.altitude +
          ',Acc:' +
          e.accuracy +
          ',Speed:' +
          e.speed +
          ',Time:' +
          e.timestamp
        setNewLoc(addressToShow)
      })
    } else {
      // Check if the native module exists before creating the NativeEventEmitter instance
      if (eventEmitter) {
        eventEmitter.addListener('significantLocationChange', data => {
          console.log('Location from IOS:', data)
        })
        //eventEmitter.start();
        //NativeModules.RNLocationChange.start()
        console.log('DeviceEventEmitter Listener added')
      } else {
        console.error("Native module 'MyNativeModule' not found.")
      }
    }

    //   Platform.OS === 'android'
    //     ? LocationServiceModule.startLocationService()
    //     : location.initialize()
  }

  const stopService = () => {
    Platform.OS === 'android' && LocationServiceModule.stopLocationService()
    //:location.stop()
    //NativeModules.RNLocationChange.stop();
  }

  const getOneTimeLocation = () => {
    console.log('getOneTimeLocation ...')
    Geolocation.getCurrentPosition(
      //Will give you the current location
      position => {
        const altitude = JSON.stringify(position.coords.altitude)
        const speed = JSON.stringify(position.coords.speed)

        setCurrentLatitude(position.coords.latitude)
        setCurrentLongitude(position.coords.longitude)
        setTimeStamp(position.timestamp)
        setAltitude(parseInt(altitude))
        setSpeed(parseInt(speed))
        console.log('Location updated', currentLatitude)
      },
      error => {
        console.log('Getting Location Error:', error.message)
      },
      {
        enableHighAccuracy: true,
        timeout: 3000000,
        maximumAge: 1000,
      },
    )
  }

  const subscribeLocationLocation = () => {
    console.log('subscribeLocation...')
    Geolocation.watchPosition(
      position => {
        //Will give you the location on location change
        const altitude = JSON.stringify(position.coords.altitude)
        const speed = JSON.stringify(position.coords.speed)

        setCurrentLatitude(position.coords.latitude)
        setCurrentLongitude(position.coords.longitude)
        setTimeStamp(position.timestamp)
        setAltitude(parseInt(altitude))
        setSpeed(parseInt(speed))
        console.log('Location updated', currentLatitude)
      },
      error => {
        console.log('Getting Frequent location Error:', error.message)
      },
      {
        enableHighAccuracy: true,
        maximumAge: 1000,
      },
    )
  }

  useEffect(() => {
    const interval = setInterval(() => {
      ;(async () => {
        if (Platform.OS === 'android') requestBackgroundPermission()
        if (Platform.OS === 'ios') {
          requestPermissions()
          if (eventEmitter) {
            eventEmitter.addListener('significantLocationChange', data => {
              console.log('Events traced:', data)
              setCurrentLatitude(parseFloat(data.coords.latitude))
              setCurrentLongitude(parseFloat(data.coords.longitude))
              setAltitude(parseFloat(data.coords.altitude))
              setAccuracy(parseFloat(data.coords.accuracy))
              setSpeed(parseFloat(data.coords.speed))
              setTimeStamp(parseInt(data.coords.timestamp))
              console.log('Location parsed')
            })
          } else {
            console.error("Native module 'MyNativeModule' not found.")
          }
        }

        //Uncomment if you need location change in reactnative
        // getOneTimeLocation()
        // subscribeLocationLocation()
        console.log('Check Location', currentLatitude, currentLongitude)
        if (
          currentLatitude != lastLatitude ||
          currentLongitude != lastLongitude
        ) {
          //todo need to Add check to save location only if location changed
          console.log('Location Changed')
          lastLatitude = currentLatitude
          lastLongitude = currentLongitude
          // use realm to interact with database
          const realm = await Realm.open({
            path: 'myrealm',
            schema: [TaskSchema],
          })

          //   // write records to database
          realm.write(() => {
            const task = realm.create('MapData', {
              lat: currentLatitude.toString(),
              long: currentLongitude.toString(),
              alt: altitude.toString(),
              direct: direction.toString(),
              accuracy: accuracy.toString(),
              spd: speed.toString(),
              timestamp: timeStamp.toString(),
              _id: Date.now(),
            })
            console.log(
              `created tasks Latitude: ${task.lat} Longitude: ${task.long}`,
            )
          })

          //   // ### read records from database
          const tasks = realm.objects('MapData')
          console.log(
            `The lists of tasks are: ${tasks.map(task => {
              return (
                task.lat +
                ',Long:' +
                task.long +
                ',Alt:' +
                task.alt +
                ',accuracy:' +
                task.accuracy +
                ',Speed:' +
                task.spd +
                ',TimeStamp:' +
                task.timestamp +
                '\n\r'
              )
            })}`,
          )
        }

        // ### read 1 record from database
        // const myTask = realm.objectForPrimaryKey("Task", 1637096347792); // search for a realm object with a primary key that is an int.
        // console.log(`got task: ${myTask.name} & ${myTask._id}`);

        // ### modify ONE record from database
        // realm.write(() => {
        //   let myTask = realm.objectForPrimaryKey("Task", 1637096347792);
        //   console.log(`original task: ${myTask.name} & ${myTask._id} ${myTask.status}`);
        //   myTask.status = "Closed"
        // });

        // ### delete ONE record from database
        // realm.write(() => {
        //   try {
        //     let myTask = realm.objectForPrimaryKey('Task', 1637096312440)
        //     realm.delete(myTask)
        //     console.log('deleted task ')
        //     myTask = null
        //   } catch (error) {
        //     console.log(error)
        //   }
        // })
      })()
    }, 5000)
    return () => clearInterval(interval)
  }, [currentLatitude, currentLongitude])

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <View style={styles.container}>
        <MapView
          provider={PROVIDER_GOOGLE} // remove if not using Google Maps
          style={styles.mapStyle}
          initialRegion={{
            latitude: 37.78825,
            longitude: -122.4324,
            latitudeDelta: 0.0922,
            longitudeDelta: 0.0421,
          }}
          zoomEnabled={true}
          zoomControlEnabled={true}
          showsUserLocation={true}
          showsMyLocationButton={true}
          followsUserLocation={true}
          onRegionChange={region => {
            setRegion(region)
            setCurrentLatitude(region.latitude)
            setCurrentLongitude(region.longitude)
          }}
          onRegionChangeComplete={region => {
            console.log('onRegionChangeComplete')
            setRegion(region)
            setCurrentLatitude(region.latitude)
            setCurrentLongitude(region.longitude)
            console.log('Region change Completed', currentLatitude)
          }}
          customMapStyle={mapStyle}
        >
          <Marker
            draggable
            coordinate={{
              latitude: currentLatitude,
              longitude: currentLongitude,
            }}
            onDragEnd={e =>
              Alert.alert(JSON.stringify(e.nativeEvent.coordinate))
            }
            title={'Test Marker'}
            description={'This is a description of the marker'}
          />
        </MapView>

        {/*Display user's current region:*/}
        <Text style={styles.text}>{newLoc}</Text>

        <View
          style={{
            flexDirection: 'row',
            justifyContent: 'space-between',
          }}
        >
          <Button title="Start Service" onPress={() => startService()} />
          <Button title="Stop Service" onPress={() => stopService()} />
        </View>
      </View>
    </SafeAreaView>
  )
}
export default App

const mapStyle = [
  { elementType: 'geometry', stylers: [{ color: '#242f3e' }] },
  { elementType: 'labels.text.fill', stylers: [{ color: '#746855' }] },
  { elementType: 'labels.text.stroke', stylers: [{ color: '#242f3e' }] },
  {
    featureType: 'administrative.locality',
    elementType: 'labels.text.fill',
    stylers: [{ color: '#d59563' }],
  },
  {
    featureType: 'poi',
    elementType: 'labels.text.fill',
    stylers: [{ color: '#d59563' }],
  },
  {
    featureType: 'poi.park',
    elementType: 'geometry',
    stylers: [{ color: '#263c3f' }],
  },
  {
    featureType: 'poi.park',
    elementType: 'labels.text.fill',
    stylers: [{ color: '#6b9a76' }],
  },
  {
    featureType: 'road',
    elementType: 'geometry',
    stylers: [{ color: '#38414e' }],
  },
  {
    featureType: 'road',
    elementType: 'geometry.stroke',
    stylers: [{ color: '#212a37' }],
  },
  {
    featureType: 'road',
    elementType: 'labels.text.fill',
    stylers: [{ color: '#9ca5b3' }],
  },
  {
    featureType: 'road.highway',
    elementType: 'geometry',
    stylers: [{ color: '#746855' }],
  },
  {
    featureType: 'road.highway',
    elementType: 'geometry.stroke',
    stylers: [{ color: '#1f2835' }],
  },
  {
    featureType: 'road.highway',
    elementType: 'labels.text.fill',
    stylers: [{ color: '#f3d19c' }],
  },
  {
    featureType: 'transit',
    elementType: 'geometry',
    stylers: [{ color: '#2f3948' }],
  },
  {
    featureType: 'transit.station',
    elementType: 'labels.text.fill',
    stylers: [{ color: '#d59563' }],
  },
  {
    featureType: 'water',
    elementType: 'geometry',
    stylers: [{ color: '#17263c' }],
  },
  {
    featureType: 'water',
    elementType: 'labels.text.fill',
    stylers: [{ color: '#515c6d' }],
  },
  {
    featureType: 'water',
    elementType: 'labels.text.stroke',
    stylers: [{ color: '#17263c' }],
  },
]
const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    alignItems: 'center',
    justifyContent: 'flex-end',
  },
  mapStyle: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
  },
  boldText: {
    fontSize: 25,
    color: 'red',
    marginVertical: 16,
    textAlign: 'center',
  },
  text: {
    fontSize: 18,
    color: '#fff',
    paddingHorizontal: 5,
    marginBottom: 45,
  },
})
