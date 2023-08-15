import React, { useEffect, useState } from 'react'
import {
  View,
  StyleSheet,
  Text,
  FlatList,
  Platform,
  Button,
} from 'react-native'
import { fetchItems, deleteAll, deleteOldData } from './SQLiteBridge' // Adjust the path accordingly

const DisplayData = ({ navigation }) => {
  const [locationData, setLocationData] = useState([])

  useEffect(() => {
    const timerId = setTimeout(() => {
      getDataFromDb()

      // Realm.open({ schema: [LocationSchema] })
      //   .then(realm => {
      //     // Use the realm instance to read data from the database
      //     const data = realm.objects('MapData')

      //     const taskArray = Array.from(data) // Convert Realm object to an array to use with FlatList or other components.
      //     console.log('Saved Locations:', taskArray.length)
      //     setLocationData(taskArray)
      //   })
      //   .catch(error => {
      //     console.error('Error opening Realm:', error)
      //   })
    }, 1000)

    // Clean up the timer when the component unmounts (optional)
    return () => {
      clearTimeout(timerId)
    }
  }, []) // Empty dependency array means the effect runs only once on component mount

  const getDataFromDb = () => {
    console.log('Fetching data from DB')

    fetchItems()
      .then(itemsArray => {
        console.log('Data fetched Successfully:', itemsArray)
        console.log('Saved Locations:', itemsArray.length)
        setLocationData(itemsArray)
      })
      .catch(error => {
        console.error('Error fetching items:', error)
      })
  }

  const deleteSelectedRecords = () => {
    deleteOldData(1)
  }

  const deleteAllData = () => {
    deleteAll()
    getDataFromDb()
  }

  return (
    <View>
      <Button title="Delete All" onPress={() => deleteAllData()} />
      <FlatList
        data={locationData}
        ItemSeparatorComponent={() => (
          <View style={{ backgroundColor: 'green', height: 2 }} />
        )}
        renderItem={({ item }) => (
          <View style={{ flex: 1, flexDirection: 'column' }}>
            <Text style={styles.textViewContainer}>
              {'Time = ' + item.timeStamps}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Latitude = ' + item.latitude}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Longitude = ' + item.longitude}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Altitude = ' + item.altitude}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Accuracy = ' + item.accuracy}
            </Text>
            <Text style={styles.textViewContainer}>
              {'Speed = ' + item.speed}
            </Text>
          </View>
        )}
      />
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
