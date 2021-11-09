define [
  'cs!models/EditorMetadata'
], (EditorMetadata) -> EditorMetadata.extend

  initialize: (data, options) ->
    EditorMetadata.prototype.initialize.apply @
    if 'id' of data
      @id = data.id
      @parameters = ''
    else
      @id = options.id
      @parameters = '?catalogue=eidc'

  urlRoot: -> "/service-agreement/#{@id}#{@parameters}"

  validate: (attrs) ->
    errors = EditorMetadata.prototype.validate.call(@, attrs) || []
    unless attrs?.depositorContactDetails?
      errors.push 'Depositor contact details are mandatory'

    if _.isEmpty errors
    # return nothing from Backbone.Model.validate because returning something signals a validation error.
      return
    else
      return errors
