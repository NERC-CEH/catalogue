define [
  'cs!views/editor/ObjectInputView'
  'cs!views/editor/MapReprojectionView'
  'cs!views/editor/ChildView'
  'cs!views/editor/MapFeaturesView'
  'cs!views/editor/MapAttributeView'
  'cs!collections/Positionable'
  'tpl!templates/editor/MapDataSource.tpl'
], (ObjectInputView, MapReprojectionView, ChildView, MapFeaturesView, MapAttributeView, Positionable, template) -> ObjectInputView.extend

  template: template

  events: ->
    _.extend {}, ObjectInputView.prototype.events,
      'click .addReprojection': 'addReprojection'
      'click .addAttribute': 'addAttribute'
  

  dataTypes:[
    {name: 'Polygon', value: 'POLYGON'}
    {name: 'Raster',  value: 'RASTER'}
    {name: 'Point',   value: 'POINT'}
    {name: 'Line',    value: 'LINE'}
  ]

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, _.extend {}, options,
      types: @dataTypes

    @reprojections = @model.getRelatedCollection 'reprojections'
    @attributes = @model.getRelatedCollection 'attributes'
    
    @createList @reprojections, '.reprojections', @newReprojection
    @createList @attributes, '.attributes', @newAttribute

    new MapFeaturesView
      el: @$('.features')
      model: @model.getRelated 'features'

  addReprojection: -> @reprojections.add {}
  addAttribute:    -> @attributes.add {}

  newReprojection: (model, i) ->
    new ChildView
      model: model
      index: i
      ObjectInputView: MapReprojectionView


  newAttribute: (model, i) ->
    new ChildView
      model: model
      index: i
      ObjectInputView: MapAttributeView