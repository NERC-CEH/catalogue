define [
  'cs!models/EditorMetadata'
], (EditorMetadata) -> EditorMetadata.extend

  urlRoot: ->
    "/service-agreement/#{@get('id')}?catalogue=eidc"
