define [
  'backbone'
  'cs!models/deims/DeimsSite'
  'cs!collections/deims/DeimsSiteCollection'
  'tpl!templates/'  #need to create a HTML template for the picker
], (Backbone, DeimsSite, DeimsSiteCollection, template) -> Backbone.View.extend

  @getSites = [

  var siteCollection = new DeimsSiteCollection;
  siteCollection.fetch();
  return SiteCollection;
  ]

  @testGetSites = [
  var sites = @getSites;

    sites.fetch({success: function(){
        console.log(sites.pluck('title'));
        }
    ]

  initialize: ->
    @getSites()
    @listenTo(@model, 'sync', @remove)
    @listenTo(@model, 'change', @render)

  select: ->
    previous = @model.get('toDelete')
    @model.set('toDelete', !previous)

  render: ->
    @$el.html(@template(@model.attributes))
    @
