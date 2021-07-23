define [
  'backbone'
  'jquery'
], (Backbone, $) -> Backbone.Model.extend

  urlRoot:
    "/upload"

#  TODO: where are pages used?
  page:
    documentsPage: 1
    datastorePage: 1
    supportingDocumentsPage: 1
