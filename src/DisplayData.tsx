import React, { useEffect, useState } from 'react'
import {
  View,
  StyleSheet,
  Pressable,
  Text,
  FlatList,
  Platform,
} from 'react-native'
import LocationSchema from './LocationScheme'
import Realm from 'realm'

const DisplayData = ({ navigation }) => {
  const [locationData, setLocationData] = useState([])

  useEffect(() => {
    const interval = setInterval(() => {
      Realm.open({ schema: [LocationSchema] })
        .then(realm => {
          // Use the realm instance to read data from the database
          const data = realm.objects('MapData')

          const taskArray = Array.from(data) // Convert Realm object to an array to use with FlatList or other components.
          console.log('Saved Locations:', taskArray.length)
          setLocationData(taskArray)

          console.log(
            `Saved Locations are: ${data.map(task => {
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
        })
        .catch(error => {
          console.error('Error opening Realm:', error)
        })
    }, 1000)
    return () => clearInterval(interval)
  }, [])

  return (
    <View>
      <FlatList
        data={locationData}
        ItemSeparatorComponent={() => (
          <View style={{ backgroundColor: 'green', height: 2 }} />
        )}
        renderItem={({ item }) => (
          <View style={{ flex: 1, flexDirection: 'column' }}>
            <Text style={styles.textViewContainer}>
              {'Time = ' + item.timestamp}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Latitude = ' + item.lat}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Longitude = ' + item.long}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Altitude = ' + item.alt}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Accuracy = ' + item.accuracy}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Speed = ' + item.spd}
            </Text>
          </View>
        )}
      />
      {/* <Pressable
        style={styles.button}
        onPress={() => navigation.navigate('home')}
      >
        <Text style={styles.TextStyle}>Go Home</Text>
      </Pressable> */}
    </View>
  )
}

const styles = StyleSheet.create({
  MainContainer: {
    flex: 1,
    justifyContent: 'center',
    color: 'green',
    paddingTop: Platform.OS === 'ios' ? 20 : 0,
    margin: 10,
  },
  button: {
    width: '60%',
    height: 40,
    padding: 10,
    alignSelf: 'center',
    backgroundColor: '#4CAF50',
    borderRadius: 7,
    marginTop: 12,
  },

  TextStyle: {
    color: '#fff',
    textAlign: 'center',
  },

  textViewContainer: {
    textAlignVertical: 'center',
    paddingHorizontal: 5,
    fontSize: 19,
    color: '#000',
  },
})
export default DisplayData
