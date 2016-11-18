define [
  'underscore'
  'cs!models/editor/NestedModel'
], (_, NestedModel) -> NestedModel.extend

  defaults:
    type: 'POLYGON'
    epsgCode: 4326
    features:
      style:
        colour: '#000000'


  initialize: ->
    NestedModel.prototype.initialize.apply @, arguments

    # Determine the current styling mode
    @stylingMode = if _.isEmpty @attributes.attributes then 'features' else 'attributes'

  ###
  Update the styling mode and trigger a change event as either the features
  or attributes will now be hidden.
  ###
  setStylingMode: (mode) -> 
    @stylingMode = mode
    @trigger 'change', @, {}

  validate: (attrs) ->
    errors = []

    # Validate all of the min and max values of any defined buckets
    numRegex = /^-?(?:\d+(?:\.\d+)?|\.\d+)$/
    if _.chain(attrs.attributes)
        .pluck('buckets')
        .flatten()
        .map((b)-> [b.min, b.max])
        .flatten()
        .select((n) -> n?)
        .any((n)-> not numRegex.test(n))
        .value()
      errors.push message: 'Bucket values must be numbers'

    return if _.isEmpty errors then undefined else errors

  ###
  Depending on the stylingMode, either hide the `features` or `attributes` 
  attributes.
  ###
  toJSON: ->
    json = NestedModel.prototype.toJSON.apply @, arguments
    switch @stylingMode
      when 'features'   then _.omit json, 'attributes'
      when 'attributes' then _.omit json, 'features'