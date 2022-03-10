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
    @._setObject objectName, attributeName, value
    return false # disable bubbling

  _setObject: (objectName, attributeName, value) ->
    if not value
      @model.unset objectName
    else
      if not _.isUndefined(attributeName)
        obj = _.extend({}, @model.get(objectName))
        obj[attributeName] = value
        @model.set objectName, obj
      else
        @model.set objectName, value