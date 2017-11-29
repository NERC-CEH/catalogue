define [
    'tpl!templates/DocumentUploadMessage.tpl'
], (documentUploadMessageTpl) ->
  (value, type, timeout) ->
    message = documentUploadMessageTpl
      type: type
      message: value
    message = $(message)

    message.find('.close').click ->
      message.hide 'fast', ->
        do message.remove
        messages.hide 'fast' if $('.messages .message').length == 0

    messages = $('.messages')
    messages.show 'fast' if !messages.is ':visible'
    messages.append message
    messages.scrollTop 30 * messages.find('.message').length

    if timeout
      setTimeout ->
        message.hide 'fast', ->
          do message.remove
          messages.hide 'fast' if $('.messages .message').length == 0
      , timeout