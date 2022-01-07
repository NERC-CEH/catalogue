define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/DataTypeSchema.tpl'
], (_, ObjectInputView, template) -> ObjectInputView.extend

  template: template


  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')  #CHECK THIS
    value = $target.val()

    if _.contains(['maximum', 'minimum', 'maxLength', 'minLength', 'unique'], name)
      constraints = _.clone @model.get 'constraints'
      if value
        constraints[name] = value
        @model.set 'constraints', constraints
      else
        constraints = _.omit constraints, name
        @model.set 'constraints', constraints
    else
      if value
        @model.set name, value
      else
        @model.unset name
