/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'underscore',
  'backbone'
], function($, _, Backbone) { return Backbone.Router.extend({
  routes: {
    "layers/:layers" : "loadLayers"
  },

  initialize(options) {
    return this.model = options.model;
  },

  /*
  Grab the list of ids from the route and set these on the app model.
  This will trigger the app to fetch the relevant representations
  */
  loadLayers(route) {
    return this.model.set('metadataIds', route.split('!'));
  }
});
 });