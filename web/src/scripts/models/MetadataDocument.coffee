define [
  'underscore'
  'backbone'
  'cs!collections/OnlineResources'
], (_, Backbone, OnlineResources) -> Backbone.Model.extend
  
  url: -> "/documents/#{@id}"

  initialize:->
    @onlineResources = new OnlineResources [], metadataDocument: this
    @on 'change:onlineResources', @_populateOnlineResources

  ###
  Return the Backbone collection of online resources
  ###
  getOnlineResources:-> @onlineResources

  ###
  When the online resources attribute of this metadata document change,
  populate the OnlineResources collection
  ###
  _populateOnlineResources:->
    onlineResources = _.map @get('onlineResources'), (e,id) -> _.extend(e, id:id)
    @getOnlineResources().reset onlineResources