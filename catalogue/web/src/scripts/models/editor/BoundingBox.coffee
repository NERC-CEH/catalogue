define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend {

  validate: (attrs) ->
    labels =
      westBoundLongitude: 'West Bounding Longitude'
      eastBoundLongitude: 'East Bounding Longitude'
      northBoundLatitude: 'North Bounding Longitude'
      southBoundLatitude: 'South Bounding Longitude'

    errors = []

    isStringANumber = (input) ->
      # coerce attribute to a number with + then check if operation produced NaN
      not isNaN(+ input)

    isGreater = (first, second) ->
      if isStringANumber(first) and isStringANumber(second)
        first = parseFloat first
        second = parseFloat second
        first > second

    isOutOfRange = (input, min, max) ->
      if isStringANumber input
        input = parseFloat input
        not (input <= max and input >= min)

    _.chain(attrs)
      .keys()
      .each (key) ->
        if not isStringANumber attrs[key]
          errors.push message: "#{labels[key]} needs to be a number"

    if isGreater attrs.westBoundLongitude, attrs.eastBoundLongitude
      errors.push
        message:
          "West Bounding Longitude should be less the East Bounding Longitude"

    if isGreater attrs.southBoundLatitude, attrs.northBoundLatitude
      errors.push
        message:
          "South Bounding Longitude should be less the North Bounding Longitude"

    if isOutOfRange attrs.westBoundLongitude, -180, 180
      errors.push
        message:
          "West Bounding Longitude should be between -180&deg; and 180&deg;"

    if isOutOfRange attrs.eastBoundLongitude, -180, 180
      errors.push
        message:
          "East Bounding Longitude should be between -180&deg; and 180&deg;"

    if isOutOfRange attrs.northBoundLatitude, -90, 90
      errors.push
        message:
          "North Bounding Longitude should be between -90&deg; and 90&deg;"

    if isOutOfRange attrs.southBoundLatitude, -90, 90
      errors.push
        message:
          "South Bounding Longitude should be between -90&deg; and 90&deg;"

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
}
