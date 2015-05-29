define [
  'backbone'
  'jquery'
], (Backbone, $) -> Backbone.Model.extend

  defaults:
    code: ''
    codeSpace: ''
    version: ''

  initialize: (options) ->
    if options?.generateCeh
      @set
        code: $.now()
        codeSpace: 'CEH:EIDC:'