define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/MapStyleSelector.tpl'
  'colorpicker'
], (_, ObjectInputView, template) -> ObjectInputView.extend

  template: template

  events:
    'changeColor':          'setColour'
    'click a[data-symbol]': 'setSymbol'

  buttonColour: '#fff'

  defaultColour: '#fff'

  symbols:
    circle: 
      icon: '⬤', label: 'Circle'
    square:
      icon: '⬛',  label: 'Square'

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, _.extend {}, options,
      symbols: @symbols

    do @update
  
    @$('input').colorpicker format: 'hex'
    @listenTo @model, 'change:colour change:symbol', @update

  update: ->
    color = @model.get 'colour'
    @$('input').val color
    if @model.has 'symbol'
      symbol = @model.get 'symbol'
      @$('.selected').html @symbols[symbol].icon
      @$('button').css backgroundColor: @buttonColour
    else
      @$('.selected').html '&nbsp;'
      @$('button').css backgroundColor: color

    @$('.icon').css color: color

  setColour: -> @model.set 'colour', @$('input').val()

  setSymbol: (e) ->
    if $(e.target).data('symbol') isnt 'blank'
      @model.set 'symbol', $(e.target).data 'symbol'
    else
      @model.unset 'symbol'
