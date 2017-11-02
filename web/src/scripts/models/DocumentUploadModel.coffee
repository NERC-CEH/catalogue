define [
  'backbone'
], (Backbone) -> Backbone.Model.extend
  urlRoot: window.location.href
  defaults:
    cancel: no
    message: off
    modal: off

  postWithFormData: (url, successMessage, errorMessage, data) ->
    formData = new FormData()
    for key, value of data
      if Array.isArray value
        for index, arrValue of value
          formData.append key + '[]', arrValue
      else
        formData.append key, value

    @save null,
      url: url
      data: formData
      processData: false
      contentType: false
      success: => @success successMessage
      error: (xhr, error) => @error error, errorMessage

  success: (message) ->
    @set 'message',
      message: message
      type: 'success'
      timeout: 3000

  error: (error, baseMessage) ->
    errorMessage =
      message: baseMessage
      type: 'warning'
      timeout: 3000
    errorMessage.message = baseMessage + ' because ' + error.responseText if error && error.responseText
    @set
      cancel: yes
      message: errorMessage