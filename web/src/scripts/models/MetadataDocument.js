/*
 * decaffeinate suggestions:
 * DS101: Remove unnecessary use of Array.from
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone',
  'cs!collections/OnlineResources'
], function(_, Backbone, OnlineResources) { return Backbone.Model.extend({
  
  url() { return `/documents/${this.id}`; },

  initialize() {
    this.onlineResources = new OnlineResources([], {metadataDocument: this});
    this.on('change:onlineResources', this.populateOnlineResources);

    // Proxy the events of the only resources through this metadata document.
    // This means that when a onlineResource is fetched. The events of the 
    // collection will bubble out of this metadata document
    return this.onlineResources.on('all', (evt, ...args) => {
      return this.trigger(`resources-${evt}`, ...Array.from(args));
    });
  },

  /*
  Return the Backbone collection of online resources
  */
  getOnlineResources() { return this.onlineResources; },

  /*
  When the online resources attribute of this metadata document change,
  populate the OnlineResources collection
  */
  populateOnlineResources() {
    const onlineResources = _.map(this.get('onlineResources'), (e, id) => _.extend(e, {id}));
    this.onlineResources.reset(onlineResources);

    // Fetch those online resources which have more information (wms)
    return this.onlineResources.chain()
                    .filter(e => e.isWms())
                    .forEach(e => (e.fetch)());
  }
});
 });