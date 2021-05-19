define [
  'backbone'
  'cs!models/deims/DeimsSite'
], (Backbone, DeimsSite) -> Backbone.Collection.extend

    url : "/vocabularies/deims"
    model: DeimsSite
    comparator: 'title'
