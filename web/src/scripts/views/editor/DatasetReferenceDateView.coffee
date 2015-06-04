define [
  'cs!views/editor/ObjectInputView'
  'cs!models/editor/DatasetReferenceDate'
  'tpl!templates/editor/DatasetReferenceDate.tpl'
  'jquery-ui/datepicker'
], (ObjectInputView, DatasetReferenceDate, template) -> ObjectInputView.extend

  ModelType: DatasetReferenceDate
  modelAttribute: 'datasetReferenceDate'
  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('input').datepicker dateFormat: "yy-mm-dd"
    @
