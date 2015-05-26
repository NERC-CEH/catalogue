define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    individualName: ''
    organisationName: ''
    email: ''
    role: ''
    address: {}