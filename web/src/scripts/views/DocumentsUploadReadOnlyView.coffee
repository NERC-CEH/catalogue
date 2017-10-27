define [
  'backbone'
  'clipboard'
  'cs!views/DocumentUploadMessage'
], (Backbone, Clipboard, message) -> Backbone.View.extend
  initialize: (options) ->
    do $('.message.loading').remove
    $('.messages').hide 'fast' if $('.messages .message').length == 0

    clipboard = new Clipboard('.copy')
    clipboard.on 'success', (e) ->
      message 'Copied to clipboard: <b>' + e.text + '</b>', 'success', 2000
    clipboard.on 'error', (e) ->
      message 'Could not copy to clipboard: <b>' + e.text + '</b>', 'warning', 2000

    $('.copy').attr('disabled', off)
