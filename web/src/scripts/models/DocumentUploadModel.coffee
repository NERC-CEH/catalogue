define [
  'backbone'
  'cs!models/EditorMetadata'
], (Backbone, EditorMetadata) -> EditorMetadata.extend
  defaults:
    cancel: no
    message: off
    modal: off
