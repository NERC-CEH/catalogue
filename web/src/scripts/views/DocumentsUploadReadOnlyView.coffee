define [
  'backbone'
  'cs!views/DocumentUploadMessage'
], (Backbone, message) -> Backbone.View.extend
  initialize: (options) ->
    do $('.message.loading').remove
    $('.messages').hide 'fast' if $('.messages .message').length == 0
