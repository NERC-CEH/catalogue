define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend {

  validate: (attrs) ->

    # from https://github.com/jzaefferer/jquery-validation/blob/043fb91da7dab0371fded5e2a9fa7ebe4c836210/src/core.js#L1180
    urlRegEx = ///
      ^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})).?)(?::\d{2,5})?(?:[/?#]\S*)?$
    ///

    isValidUrl = (url) ->
      urlRegEx.test url

    errors = []

    if attrs.url && !isValidUrl attrs.url
      errors.push
        message:
          "Are you sure that web address is correct?"

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
}
