define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

 defaults:
  id: 'textarea'
  name: 'Textarea'
  required: ''
  rows: 17
  value: ''
  help: ''