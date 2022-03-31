import _ from 'underscore'
import Backbone from 'backbone'

export default Backbone.Model.extend({

  hasBoundingBox () {
    return this.has('westBoundLongitude') &&
            this.has('southBoundLatitude') &&
            this.has('eastBoundLongitude') &&
            this.has('northBoundLatitude')
  },

  getBoundingBox () {
    return [this.get('westBoundLongitude'), this.get('southBoundLatitude'), this.get('eastBoundLongitude'), this.get('northBoundLatitude')]
  },

  validate (attrs) {
    const labels = {
      westBoundLongitude: 'West Bounding Longitude',
      eastBoundLongitude: 'East Bounding Longitude',
      northBoundLatitude: 'North Bounding Latitude',
      southBoundLatitude: 'South Bounding Latitude'
    }

    const errors = []

    const isStringANumber = input => // coerce attribute to a number with + then check if operation produced NaN
      !isNaN(+input)

    const isGreater = function (first, second) {
      if (isStringANumber(first) && isStringANumber(second)) {
        first = parseFloat(first)
        second = parseFloat(second)
        return first > second
      }
    }

    const isOutOfRange = function (input, min, max) {
      if (isStringANumber(input)) {
        input = parseFloat(input)
        return !((input <= max) && (input >= min))
      }
    }

    _.chain(attrs)
      .keys()
      .each(function (key) {
        if (!isStringANumber(attrs[key])) {
          return errors.push({ message: `${labels[key]} needs to be a number` })
        }
      })

    if (isGreater(attrs.westBoundLongitude, attrs.eastBoundLongitude)) {
      errors.push({
        message:
                    'West Bounding Longitude should be less the East Bounding Longitude'
      })
    }

    if (isGreater(attrs.southBoundLatitude, attrs.northBoundLatitude)) {
      errors.push({
        message:
                    'South Bounding Longitude should be less the North Bounding Longitude'
      })
    }

    if (isOutOfRange(attrs.westBoundLongitude, -180, 180)) {
      errors.push({
        message:
                    'West Bounding Longitude should be between -180&deg; and 180&deg;'
      })
    }

    if (isOutOfRange(attrs.eastBoundLongitude, -180, 180)) {
      errors.push({
        message:
                    'East Bounding Longitude should be between -180&deg; and 180&deg;'
      })
    }

    if (isOutOfRange(attrs.northBoundLatitude, -90, 90)) {
      errors.push({
        message:
                    'North Bounding Longitude should be between -90&deg; and 90&deg;'
      })
    }

    if (isOutOfRange(attrs.southBoundLatitude, -90, 90)) {
      errors.push({
        message:
                    'South Bounding Longitude should be between -90&deg; and 90&deg;'
      })
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.

    } else {
      return errors
    }
  }
})
