define [
  'underscore'
  'cs!models/editor/Contact'
], (_, Contact) -> Contact.extend

  defaults: _.extend({}, Contact.prototype.defaults, {role: 'author'})

intialize: ->
  Contact.prototype.initialize.apply @
