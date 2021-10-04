define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ServiceAgreementAuthor.tpl'
], (_, ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select.role').val @model.get 'role'
    @

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if value
      @model.set name, value
    else
      @model.unset name