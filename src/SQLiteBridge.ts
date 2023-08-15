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
