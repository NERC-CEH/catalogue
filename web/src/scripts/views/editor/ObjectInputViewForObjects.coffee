define [
  'underscore'
  'cs!views/editor/ObjectInputView'
], (_, ObjectInputView) -> ObjectInputView.extend

  # Copes with an object made up of objects
  # So, one further level of objects than ObjectInputView
  # The template <input data-name="objectName.attributeName" …
  modify: (event) ->  
    $target = $(event.target)
    # TODO: Need to decide if there is an object, then split name, else carry on as 
    # in prototype method 
    [objectName, attributeName] = $target.data('name').split('.')
    value = $target.val()

    if not value
      @model.unset objectName
    else
      obj = _.extend({}, @model.get(objectName))
      obj[attributeName] = value
      @model.set objectName, obj

    return false # disable bubbling