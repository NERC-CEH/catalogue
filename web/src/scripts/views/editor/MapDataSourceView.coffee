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
      'click [styleMode]':      'updateStyleMode'
  
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

    @setStyleMode @model.stylingMode

    @listenTo @model, 'change:type', @handlerByteTypeVisibility

    # Set the radio button to the byteType of the model
    do @updateByteRadioButton

  addReprojection: -> @reprojections.add {}
  addAttribute:    -> @attributes.add {}

  newReprojection: (model, i) -> 
    new MapReprojectionView 
      model: model
      disabled: @data.disabled

  newAttribute: (model, i) ->
    new ChildView
      model: model
      index: i
      ObjectInputView: MapAttributeView
      disabled: @data.disabled

  updateStyleMode: (e) -> @setStyleMode $(e.target).attr 'styleMode'
    
  setStyleMode: (mode) ->
    # Reset the state of all the styling buttons and update to the correct mode
    @$('button[stylemode]').removeClass('btn-success').removeClass 'active'
    @$("button[stylemode='#{mode}']").addClass('btn-success active')
    @$('.styling-box').hide()
    @$(".styling-box.#{mode}").show()
    @updateByteTypeVisibility mode, @model.get('type')

    attrBtn = @$('.addAttribute').removeClass 'disabled'
    attrBtn.addClass('disabled') if mode is 'features'
    @model.setStylingMode mode

  handlerByteTypeVisibility: (e) -> @updateByteTypeVisibility e.stylingMode, e.attributes.type

  ###
  Update the bytetype radio button to match the model
  ###
  updateByteRadioButton: ->
    $(@$("input[data-name='bytetype'][value='#{@model.attributes.bytetype}']")[0]).attr('checked', 'checked')

  ###
  Set the visibility of the byteType selector.  Only show it when the styling is 'attributes' and the type is 'RASTER' 
  ###
  updateByteTypeVisibility: (stylingMode, type) ->
    if stylingMode.toLowerCase() is 'attributes' and type.toLowerCase() is 'raster' then @$('.byte-box').show() else @$('.byte-box').hide()
