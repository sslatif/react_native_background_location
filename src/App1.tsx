import React from 'react'
import {
  Text,
  StatusBar,
  View,
  StyleSheet,
  Platform,
  Button,
} from 'react-native'
import { NativeModules } from 'react-native'
const { LocationServiceModule } = NativeModules

const App = () => {
  const startService = () => {
    Platform.OS === 'android' && LocationServiceModule.startLocationService()
  }

  const stopService = () => {
    Platform.OS === 'android' && LocationServiceModule.stopLocationService()
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor={'#e4e5ea'} />
      <Text style={styles.title}>Get Location Updates</Text>
      <Text style={styles.title}> Even app is in background or killed</Text>
      <View style={styles.iconsContainer}>
        <Button title="Start Service" onPress={() => startService()} />
        <Button title="Stop Service" onPress={() => stopService()} />
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#e4e5ea',
    flex: 1,
    paddingTop: 50,
    paddingHorizontal: 20,
    alignItems: 'center',
  },
  title: {
    fontSize: 20,
    color: '#000',
    marginVertical: 15,
  },
  iconsContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-evenly',
    width: '100%',
    paddingHorizontal: 50,
  },
  warningText: {
    color: 'red',
    fontWeight: 'bold',
    letterSpacing: 1.5,
    textAlign: 'center',
  },
  spacing: {
    marginVertical: 10,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '40%',
  },
  icon: {
    height: 40,
    width: 40,
    marginBottom: 15,
  },
})

export default App
