import React, { useState, useEffect } from 'react'
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
import { NativeModules, NativeEventEmitter } from 'react-native'
import LocationSchema from './LocationScheme'
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
    const locationGranted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      // PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
      {
        title: 'Access Location Permission',
        message:
          'We need access to your location ' +
          'so you can get live quality updates.',
        buttonNeutral: 'Ask Me Later',
        buttonNegative: 'Cancel',
        buttonPositive: 'OK',
      },
    )
    if (locationGranted === PermissionsAndroid.RESULTS.GRANTED) {
      if (Number(Platform.Version) >= 29) {
        const backgroundgranted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
          {
            title: 'Background Location Permission',
            message:
              'We need access to your location ' +
              'so you can get live quality updates.',
            buttonNeutral: 'Ask Me Later',
            //buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          },
        )
        if (backgroundgranted === PermissionsAndroid.RESULTS.GRANTED) {
          //console.log('Background location permission granted')
        } else {
          console.log('Background location permission denied')
        }
      } else {
        console.log(
          'Background location permission is not required on this Android version.',
        )
      }
    } else {
      console.log('Access location permission denied')
    }
  } catch (error) {
    console.error(
      'Error occurred while requesting background location permission:',
      error,
    )
  }
}

const HomeScreen = ({ navigation }) => {
  const [currentLatitude, setCurrentLatitude] = useState(0.0)
  const [currentLongitude, setCurrentLongitude] = useState(0.0)
  const [altitude, setAltitude] = useState(0.0)
  const [accuracy, setAccuracy] = useState(0.0)
  const [speed, setSpeed] = useState(0.0)
  const [timeStamp, setTimeStamp] = useState(0)

  const [direction, setDirection] = useState('')
  const [state, setState] = useState({ events: [] })
  const [newLoc, setNewLoc] = useState('0.0')

  const [lastLatitude, setLastLatitude] = useState(0.0)
  const [lastLongitude, setLastLongitude] = useState(0.0)
  const [diffInSeconds, setDiffInSeconds] = useState(5)
  const [distanceInMeter, setDistanceInMeter] = useState(0.0)
  const [lastEnteryTime, setLastEnteryTime] = useState(Date.now())
  const [lastRecordId, setLastRecordId] = useState(-1)

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
        //console.log('DeviceEventEmitter Location Listener', e)
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
          formatTimestampToDateTime(parseInt(e.timestamp))
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

  const showMapData = () => {
    navigation.navigate('locationdata', {
      paramKey: 'locationData',
    })
  }

  const formatTimestampToDateTime = newTimestamp => {
    // Create a new Date object using the timestamp (converting from milliseconds to seconds)
    const dateObject = new Date(newTimestamp)

    // Extract the date and time components from the Date object
    const year = dateObject.getFullYear()
    const month = dateObject.getMonth() + 1 // Months are zero-based, so add 1
    const day = dateObject.getDate()
    const hours = dateObject.getHours()
    const minutes = dateObject.getMinutes()
    const seconds = dateObject.getSeconds()

    // Create a formatted date and time string
    const formattedDateTime = `${year}-${month
      .toString()
      .padStart(2, '0')}-${day.toString().padStart(2, '0')} ${hours
      .toString()
      .padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds
      .toString()
      .padStart(2, '0')}`

    //console.log('New date:', formattedDateTime)
    return formattedDateTime
  }

  const getDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371 // Earth's radius in kilometers
    //const R = 6371e3 // Earth's radius in meters

    const toRadians = angle => {
      return angle * (Math.PI / 180)
    }

    const dLat = toRadians(lat2 - lat1)
    const dLon = toRadians(lon2 - lon1)
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRadians(lat1)) *
        Math.cos(toRadians(lat2)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    const distance = R * c * 1000

    return distance // distance in meters
  }

  const getTimeDifferenceInMilliseconds = (timestamp1, timestamp2) => {
    const date1 = new Date(timestamp1)
    const date2 = new Date(timestamp2)

    // Calculate the time difference in milliseconds
    const differenceInMilliseconds = Math.abs(date2.getTime() - date1.getTime())

    return differenceInMilliseconds
  }

  const checkAndStoreData = () => {
    setDistanceInMeter(
      getDistance(
        lastLatitude,
        lastLongitude,
        currentLatitude,
        currentLongitude,
      ),
    )

    setDiffInSeconds(getTimeDifferenceInMilliseconds(lastEnteryTime, timeStamp))

    if (distanceInMeter > 5 && diffInSeconds > 30000) {
      setDiffInSeconds(0)
      console.log(
        `*************  TIME RESET  ************* SEC: ${diffInSeconds} Dist: ${distanceInMeter}`,
      )
      setLastEnteryTime(Date.now())

      setLastLatitude(currentLatitude)
      setLastLongitude(currentLongitude)
      if (currentLatitude != 0 && currentLongitude != 0) writeDataToRealm()
    } else {
      console.log(
        `Distance=${distanceInMeter.toFixed(
          3,
        )} | Time=${diffInSeconds} | Latitude=${currentLatitude} | Longitude= ${currentLongitude} | LastLat=${lastLatitude} | lastLong=${lastLongitude}`,
      )
    }
  }

  const writeDataToRealm = () => {
    if (lastRecordId == Date.now()) {
      console.log('Record already exist!!')
    } else {
      var dateTime = new Date().toLocaleString()
      setLastRecordId(Date.now())
      Realm.open({ schema: [LocationSchema] })
        .then(realm => {
          realm.write(() => {
            const task = realm.create('MapData', {
              lat: currentLatitude.toString(),
              long: currentLongitude.toString(),
              alt: altitude.toString(),
              direct: direction.toString(),
              accuracy: accuracy.toString(),
              spd: speed.toString(),
              timestamp: dateTime,
              _id: lastRecordId === -1 ? Date.now() : lastRecordId,
            })
            console.log(
              `created tasks Home:Id:${lastRecordId} Lat:${task.lat} Long: ${task.long} Time: ${task.timestamp}`,
            )
          })
        })
        .catch(error => {
          console.error('Error opening Realm:', error)
        })
    }
  }

  useEffect(() => {
    checkAndStoreData()
  }, [currentLatitude, currentLongitude, accuracy, altitude, speed, timeStamp])

  useEffect(() => {
    //Check and Save data timebased
    const interval = setInterval(() => {
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

            addressToShow =
              'LatLng:' +
              data.coords.latitude +
              ',' +
              data.coords.longitude +
              ',Alt:' +
              data.coords.altitude +
              ',Acc:' +
              data.coords.accuracy +
              ',Speed:' +
              data.coords.speed +
              ',Time:' +
              data.coords.timestamp
            setNewLoc(addressToShow)
            console.log('Address to show:IOS', addressToShow)
          })
        } else {
          console.error("Native module 'MyNativeModule' not found.")
        }
      }
      //checkAndStoreData()
    }, 1000)
    return () => clearInterval(interval)
  }, [])

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
            //setCurrentLatitude(region.latitude)
            //setCurrentLongitude(region.longitude)
          }}
          onRegionChangeComplete={region => {
            setRegion(region)
            setCurrentLatitude(parseFloat(region.latitude.toFixed(4)))
            setCurrentLongitude(parseFloat(region.longitude.toFixed(4)))
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
        <View style={styles.buttonsContainer}>
          <Button title="Start Service" onPress={() => startService()} />
          <Button title="Stop Service" onPress={() => stopService()} />
          <Button title="Locations" onPress={() => showMapData()} />
        </View>
      </View>
    </SafeAreaView>
  )
}
export default HomeScreen

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
    fontSize: 15,
    color: '#fff',
    marginHorizontal: 5,
    marginBottom: Platform.OS === 'android' ? 45 : 5,
  },
  buttonsContainer: {
    width: '95%',
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    marginBottom: Platform.OS === 'android' ? 2 : 60,
  },
})
