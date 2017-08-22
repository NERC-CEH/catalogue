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

    errors = []

    organisationName = attrs.organisationName
    role = attrs.role
    email = attrs.email

    isValidEmail = (address) ->
      emailRegEx.test address

    if !isValidEmail email
      errors.push
        message:
          "Not a valid email address"

    if ! organisationName || ! email || ! role
      errors.push
        message: 'Organisation name, role and email are mandatory'

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors


  toJSON: ->
    if _.isEmpty @get 'address'
      return _.omit @attributes, 'address'
    else
      return _.clone @attributes