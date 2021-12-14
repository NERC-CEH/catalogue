define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/service-agreement/Author.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template
  
  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if value
      @model.set 'role', 'author'
      @model.set name, value
    else
      @model.unset name