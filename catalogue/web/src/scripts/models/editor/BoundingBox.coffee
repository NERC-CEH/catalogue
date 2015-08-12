define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend {

  validate: (attrs) ->
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
          errors.push message: "#{key} needs to be a number"

    if isGreater attrs.westBoundLongitude, attrs.eastBoundLongitude
      errors.push
        message: "westBoundLongitude should be less the eastBoundLongitude"

    if isGreater attrs.southBoundLatitude, attrs.northBoundLatitude
      errors.push
        message: "southBoundLatitude should be less the northBoundLatitude"

    if isOutOfRange attrs.westBoundLongitude, -180, 180
      errors.push
        message: "westBoundLongitude should be between -180&deg; and 180&deg;"

    if isOutOfRange attrs.eastBoundLongitude, -180, 180
      errors.push
        message: "eastBoundLongitude should be between -180&deg; and 180&deg;"

    if isOutOfRange attrs.northBoundLatitude, -90, 90
      errors.push
        message: "northBoundLatitude should be between -90&deg; and 90&deg;"

    if isOutOfRange attrs.southBoundLatitude, -90, 90
      errors.push
        message: "southBoundLatitude should be between -90&deg; and 90&deg;"

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
}
