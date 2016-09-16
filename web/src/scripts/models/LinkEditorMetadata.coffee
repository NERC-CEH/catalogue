define [
  'cs!models/EditorMetadata'
], (EditorMetadata) -> EditorMetadata.extend

  initialize: ->
    EditorMetadata.prototype.initialize.apply @, arguments
    @unset 'original'

  validate: (attrs) ->
    return