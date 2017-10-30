define [
  'backbone'
], (Backbone) -> Backbone.Model.extend
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
      success: (xhr, res) => @success successMessage
      error: (xhr, error) => @error error, errorMessage

  success: (message) ->
    @set 'message',
      message: message
      type: 'success'
      timeout: 3000

  error: (error, baseMessage) ->
    error =
      message: baseMessage
      type: 'warning'
      timeout: 3000
    error.message = baseMessage + ' because ' + error.responseText if error.responseText
    @set
      cancel: yes
      message: error