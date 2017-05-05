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
      'click [isByte]':         'updateIsByte'
  
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

    @styleIsByte @model.get('isByte')
    @setStyleMode @model.stylingMode

    @listenTo @model, 'change:type', @handlerIsByteVisibility

  addReprojection: -> @reprojections.add {}
  addAttribute:    -> @attributes.add {}

  newReprojection: (model, i) -> new MapReprojectionView model: model
  newAttribute: (model, i) ->
    new ChildView
      model: model
      index: i
      ObjectInputView: MapAttributeView

  updateStyleMode: (e) -> @setStyleMode $(e.target).attr 'styleMode'
    
  setStyleMode: (mode) ->
    # Reset the state of all the styling buttons and update to the correct mode
    @$('button[stylemode]').removeClass('btn-success').removeClass 'active'
    @$("button[stylemode='#{mode}']").addClass('btn-success active')
    @$('.styling-box').hide()
    @$(".styling-box.#{mode}").show()
    @updateIsByteVisibility mode, @model.get('type')

    attrBtn = @$('.addAttribute').removeClass 'disabled'
    attrBtn.addClass('disabled') if mode is 'features'
    @model.setStylingMode mode

  handlerIsByteVisibility: (e) -> @updateIsByteVisibility e.stylingMode, e.attributes.type

  ###
  Set the visibility of the isByte selector.  Only show it when the styling is 'attributes' and the type is 'RASTER' 
  ###
  updateIsByteVisibility: (stylingMode, type) ->
    if stylingMode is 'attributes' and type is 'RASTER' then @$('.byte-box').show() else @$('.byte-box').hide()

  ###
  Update isByte on the model and style the isByte buttons to match
  ###
  updateIsByte: (e) -> 
    isByte = $(e.target).attr 'isByte'
    @model.set 'isByte', isByte
    @styleIsByte isByte

  ###
  Style the isByte buttons accordingly
  ###
  styleIsByte: (isByte) -> 
    @$('button[isByte]').removeClass('btn-success').removeClass 'active'
    @$("button[isByte='#{isByte}']").addClass('btn-success active')
    
