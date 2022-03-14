define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ServiceType.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  className: 'component component--servicetype visible'

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select').val @model.get @data.modelAttribute