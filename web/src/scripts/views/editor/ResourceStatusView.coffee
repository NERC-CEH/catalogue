define [
  'jquery'
  'cs!views/editor/InputView'
  'tpl!templates/editor/ResourceStatus.tpl'
], ($, InputView, template) -> InputView.extend

  template: template

  render: ->
    InputView.prototype.render.apply @
    $select = @$('select')
    $select.val @model.get @data.modelAttribute
    disabled = $($('body')[0]).data('edit-restricted')
    $select.prop disabled, disabled
    @