define [
  'underscore'
  'cs!views/editor/ObjectInputView'
], (_, ObjectInputView) -> ObjectInputView.extend

  # Copes with an object made up of objects
  # So, one further level of objects than ObjectInputView
  # The template <input data-name="objectName.attributeName" …
  modify: (event) ->
    $target = $(event.target)
    [objectName, attributeName] = $target.data('name').split('.')
    value = $target.val()

    if not value
      @model.unset objectName
    else
      attribute = @model.get(objectName)
      if _.isObject(attribute)
        obj = _.extend({}, @model.get(objectName))
        obj[attributeName] = value
        @model.set objectName, obj
      else
        @model.set objectName, value

    return false # disable bubbling