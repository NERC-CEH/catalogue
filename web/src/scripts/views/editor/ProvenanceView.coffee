define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Provenance.tpl'
], (_, ObjectInputView, template) -> ObjectInputView.extend

  template: template
  
  render: ->
    ObjectInputView.prototype.render.apply @
    @$('.datepicker').datepicker dateFormat: "yy-mm-dd" 

