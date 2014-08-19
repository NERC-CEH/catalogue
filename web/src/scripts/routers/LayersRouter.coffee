define [
  'jquery'
  'underscore'
  'backbone'
  'cs!models/MetadataDocument'
], ($, _, Backbone, MetadataDocument) -> Backbone.Router.extend
  routes:
    "layers/:layers" : "loadLayers"

  initialize: (options) ->
    @model = options.model

  loadLayers: (route) ->
    # Map each metadata id to a metadata document
    metadataRecords = _.map route.split('!'), (id) -> new MetadataDocument id: id

    # Fetch each metadata record and then each wms resource. Finally add these
    @_fetchAll(metadataRecords)
      .then => 
        wmsResources = @wmsResourcesOf metadataRecords
        @_fetchAll(wmsResources).then => @setLayers wmsResources

  ###
  Given a set of populated Metadata Records, obtain a list of the 
  onlineResources which represent wms resources
  ###
  wmsResourcesOf: (metadataRecords) ->
    _.chain(metadataRecords)
      .map((record) -> record.getOnlineResources().getWmsResources())
      .flatten()
      .value()
    
  ###
  Register each wms layer inside a wms resource as a layer in the Layers collection
  ###
  setLayers: (wmsResources) ->
    layers = _.chain(wmsResources)
      .map( (wms)-> wms.getLayers())
      .flatten()
      .value()

    @model.getLayers().reset layers

  ###
  Perform a fetch operation on an array of models. Execute the complete method
  once all of the models have been fetched.
  ###
  _fetchAll: (arr)->
    $.when
     .apply($, _.map arr, (model) -> do model.fetch)