define [
  'underscore'
  'cs!models/editor/Contact'
], (_, Contact) -> Contact.extend

  defaults: _.extend({}, Contact.prototype.defaults, {role: 'pointOfContact'})

intialize: ->
  Contact.prototype.initialize.apply @
