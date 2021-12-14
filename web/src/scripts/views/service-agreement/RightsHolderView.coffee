define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/service-agreement/RightsHolder.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if value
      @model.set 'role', 'rightsHolder'
      @model.set name, value
    else
      @model.unset name