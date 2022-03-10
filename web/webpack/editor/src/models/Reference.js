define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend {

  validate: (attrs) ->

    urlRegEx = ///
      ^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})).?)(?::\d{2,5})?(?:[/?#]\S*)?$
    ///
    DOIRegEx = ///
      ^https?:\/\/doi\.org\/10.\d{4,9}\/[-._;()/:A-Za-z0-9]+$
    ///

    isValidDOI = (doi) ->
      DOIRegEx.test doi

    isValidURL = (nora) ->
      urlRegEx.test nora

    errors = []
    
    if attrs.doi && !isValidDOI attrs.doi
      errors.push
        message:
          "Invalid DOI url"

    if attrs.nora && !isValidURL attrs.nora
      errors.push
        message:
          "Invalid NORA URL"

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
}
