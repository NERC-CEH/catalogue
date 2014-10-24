define [
  'underscore'
  'backbone'
  'cs!collections/OnlineResources'
], (_, Backbone, OnlineResources) -> Backbone.Model.extend
  
  url: -> "/documents/#{@id}"

  initialize:->
    @onlineResources = new OnlineResources [], metadataDocument: this
    @on 'change:onlineResources', @populateOnlineResources

    # Proxy the events of the only resources through this metadata document.
    # This means that when a onlineResource is fetched. The events of the 
    # collection will bubble out of this metadata document
    @onlineResources.on 'all', (evt, args...) =>
      @trigger "resources-#{evt}", args...

  ###
  Return the Backbone collection of online resources
  ###
  getOnlineResources:-> @onlineResources

  ###
  When the online resources attribute of this metadata document change,
  populate the OnlineResources collection
  ###
  populateOnlineResources:->
    onlineResources = _.map @get('onlineResources'), (e,id) -> _.extend(e, id:id)
    @onlineResources.reset onlineResources

    # Fetch those online resources which have more information (wms)
    @onlineResources.chain()
                    .filter  (e) -> e.isWms()
                    .forEach (e) -> do e.fetch