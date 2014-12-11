define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  initialize:->
    console.log 'Editor App'
