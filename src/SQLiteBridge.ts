import { NativeModules } from 'react-native'
const { LocationServiceModule } = NativeModules

//const { SQLiteModule } = NativeModules

export const fetchItems = () => {
  return new Promise((resolve, reject) => {
    LocationServiceModule.fetchItems(
      itemsArray => resolve(itemsArray),
      error => reject(error),
    )
  })
}

export const deleteOldData = id => {
  // Call the deleteOldData method
  LocationServiceModule.deleteOldData(id)
    .then(() => {
      console.log('Selected data deleted successfully.')
    })
    .catch(error => {
      console.error('Error deleting selected data:', error)
    })
}

export const deleteAll = () => {
  // Call the deleteOldData method
  LocationServiceModule.deleteAllData()
    .then(() => {
      console.log('All data deleted successfully.')
    })
    .catch(error => {
      console.error('Error deleting all data:', error)
    })
}
