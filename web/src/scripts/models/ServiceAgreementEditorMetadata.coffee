define [
  'underscore'
  'backbone'
  'cs!models/EditorMetadata'
], (_, Backbone, EditorMetadata) -> EditorMetadata.extend

  urlRoot: ->
    "/service-agreement/#{@get('id')}?catalogue=eidc"
