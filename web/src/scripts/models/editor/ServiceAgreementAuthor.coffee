define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.Model.extend

  defaults:
    address: {}

  validate: (attrs) ->

    emailRegEx = ///
      ^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$
    ///
    orcidRegEx = ///
      ^https?:\/\/orcid.org\/(\d{4}-){3}\d{3}[\dX]$
    ///

    errors = []

    organisationName = attrs.organisationName
    email = attrs.email
    individualName = attrs.individualName
    nameIdentifier = attrs.nameIdentifier

    isValidEmail = (address) ->
      emailRegEx.test address

    isValidORCID = (id) ->
      orcidRegEx.test id
    

    if email && !isValidEmail email
      errors.push
        message:
          "That email address is invalid"
    
    if nameIdentifier && !isValidORCID nameIdentifier
      errors.push
        message:
          "That ORCiD is invalid.  ORCiDs should be entered as https://orcid.org/0000-1234-5678-999X <b>not</b> 0000-1234-5678-999X"
  
    if ! organisationName || ! individualName  || ! email
      errors.push
        message: "Author name, affiliation and email address are mandatory."

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors

    if _.isEmpty warnings
      return
    else
      return warnings

  toJSON: ->
    if _.isEmpty @get 'address'
      return _.omit @attributes, 'address'
    else
      return _.clone @attributes