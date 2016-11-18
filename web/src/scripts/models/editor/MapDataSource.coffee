define [
  'underscore'
  'cs!models/editor/NestedModel'
], (_, NestedModel) -> NestedModel.extend

  initialize: ->
    NestedModel.prototype.initialize.apply @, arguments

    @stylingMode = if _.isEmpty @attributes.attributes then 'features' else 'attributes'

  setStylingMode: (mode) -> @stylingMode = mode

  toJSON: ->
    json = NestedModel.prototype.toJSON()
    switch @stylingMode
      when 'features'   then _.omit json, 'attributes'
      when 'attributes' then _.omit json, 'features'