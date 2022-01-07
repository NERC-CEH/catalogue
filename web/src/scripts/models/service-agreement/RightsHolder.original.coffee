define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.Model.extend

  defaults:
    address: {}

  validate: (attrs) ->

    rorRegEx = ///
      ^https?:\/\/ror.org\/\w{8,10}$
    ///

    errors = []

    organisationName = attrs.organisationName
    organisationIdentifier =attrs.organisationIdentifier

    isValidROR = (id) ->
      rorRegEx.test id

    if organisationIdentifier && !isValidROR organisationIdentifier
      errors.push
        message:
          "That RoR is invalid "

    if ! organisationName
      errors.push
        message: "Affiliation (organisation name) is  mandatory."

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