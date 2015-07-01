define [
  'backbone'
  'jquery'
], (Backbone, $) -> Backbone.Model.extend

  generateIdentifier: ->
    @set
      code: $.now()
      codeSpace: 'CEH:EIDC:'