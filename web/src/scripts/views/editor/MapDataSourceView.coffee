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
      'click .addAttribute':    'addAttribute'
      'click [styleMode]':      'updateMode'
  
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
    @attributes = @model.getAttributes()
    
    @createList @reprojections, '.reprojections', @newReprojection
    @createList @attributes, '.attributes', @newAttribute

    new MapFeaturesView
      el: @$('.features')
      model: @model.getRelated 'features'

    @setMode @model.stylingMode

  addReprojection: -> @reprojections.add {}
  addAttribute:    -> @attributes.add {}

  newReprojection: (model, i) -> new MapReprojectionView model: model
  newAttribute: (model, i) ->
    new ChildView
      model: model
      index: i
      ObjectInputView: MapAttributeView

  updateMode: (e) -> @setMode $(e.target).attr 'styleMode'
    
  setMode: (mode)->
    # Reset the state of all the buttons and update to the correct mode
    @$('button[stylemode]').removeClass('btn-success').removeClass 'active'
    @$("button[stylemode='#{mode}']").addClass('btn-success active')
    @$('.styling-box').hide()
    @$(".styling-box.#{mode}").show()

    attrBtn = @$('.addAttribute').removeClass 'disabled'
    attrBtn.addClass('disabled') if mode is 'features'
    @model.setStylingMode mode