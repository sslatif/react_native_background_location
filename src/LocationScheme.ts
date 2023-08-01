const LocationSchema = {
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

export default LocationSchema
