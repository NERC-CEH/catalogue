define [
  'cs!views/editor/ObjectInputView'
  'cs!views/editor/MapStyleSelectorView'
  'tpl!templates/editor/MapValue.tpl'
], (ObjectInputView, MapStyleSelectorView, template) -> ObjectInputView.extend

  template: template

  initialize: (options) ->    
    ObjectInputView.prototype.initialize.call @, options

    new MapStyleSelectorView
      el: @$('.style-selector')
      model: @model.getRelated 'style'
      disabled: options.disabled
