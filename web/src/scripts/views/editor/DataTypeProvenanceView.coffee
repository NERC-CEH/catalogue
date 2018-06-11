define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/DataTypeProvenance.tpl'
  'cs!views/editor/ParentStringView'
], (ObjectInputView, template, ParentStringView) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('input').datepicker dateFormat: "yy-mm-dd"

    new ParentStringView
        el: @$('#provenanceContributors')
        model: @model
        modelAttribute: 'contributors'
        label: 'Contributors'
    @

  ## LOOK HERE!