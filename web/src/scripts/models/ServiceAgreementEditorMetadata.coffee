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
