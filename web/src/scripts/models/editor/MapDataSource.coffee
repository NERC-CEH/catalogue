define [
  'underscore'
  'cs!models/editor/NestedModel'
], (_, NestedModel) -> NestedModel.extend

  defaults:
    type: 'POLYGON'

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

    epsgRegex = /^[0-9]+$/

    if not (epsgRegex.test(attrs.epsgCode) and 
          _.all attrs.reprojections or [], (r) -> epsgRegex.test r.epsgCode)
      errors.push message: 'EPSG Code must be a number'

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