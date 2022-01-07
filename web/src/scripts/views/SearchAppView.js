/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'jquery-deparam',
  'cs!views/SearchFormView',
  'cs!views/SpatialSearchView',
  'cs!views/SearchPageView',
  'cs!views/DrawingControlView',
  'cs!views/FacetsPanelView'
], function($, Backbone, deparam, SearchFormView, SpatialSearchView, SearchPageView, DrawingControlView, FacetsPanelView) { return Backbone.View.extend({
  el: '#search',

  events: {
    "click .facet-heading h3": "disableSearchMap",
    "click .map-heading h3":   "enableSearchMap"
  },

  initialize(options){
    this.appUrl = window.location.href.split('#')[0].split('?')[0];

    // Mutate the events hash so that is listens to clicks of urls which will
    // update the state of this web application
    this.events[`click a[href='${this.appUrl}']`] = 'defaultState';
    this.events[`click a[href^='${this.appUrl}?']`] = 'handleUrl';
    this.delegateEvents(this.events); // Register the mutated events object

    (this.render)();
    this.listenTo(this.model, 'change:mapsearch', this.updateSearchMap);
    return (this.updateFilters)();
  },


  /*
  The Sample Archive (and possibly other catalogues) does not have any facets
  to filter by.  So make the Map Search the default tab when there are no facets
  and hide the Filter search
  */
  updateFilters() {
    if (!$($('.facet-filter')[0]).has("h3").length) {
      (this.enableSearchMap)();
      (this.$('.facet-heading').hide)();
      this.$('.map-filter').css('top', this.$('.map-heading').css('height'));
      return this.$('.map-heading').css('margin-top', '0px');
    }
  },

  /*
  Grab the query state from the anchors clicked href. Use this to update the
  models state
  */
  handleUrl(e) {
    const query = $(e.currentTarget).attr('href').split('?')[1];
    this.model.setState(deparam(query, true));
    return (e.preventDefault)();
  },

  /*
  Set an empty state object on the model. This will revert the application to
  the default state.
  */
  defaultState(e){ 
    this.model.setState({});
    return (e.preventDefault)();
  },

  /*
  Disable the search map on the model and therefore enable the facet view
  */
  disableSearchMap() { return this.model.set('mapsearch', false); },

  /*
  Enable the search map on the model. This will hide the facet view
  */
  enableSearchMap() { return this.model.set('mapsearch', true); },

  /*
  Update the state of this view, we will either start showing the search map or
  the facet pane
  */
  updateSearchMap() {
    const newMode = this.model.get('mapsearch') ? 'map-mode' : 'facets-mode';
    this.$el.removeClass('facets-mode').removeClass('map-mode').addClass(newMode);
    return (this.spatialSearchView.refresh)();
  },

  /*
  Create the sub views of the search web application
  */
  render() {
    this.searchFormView = new SearchFormView({
      model: this.model,
      el:    this.$('.search-form')
    });
      
    this.spatialSearchView = new SpatialSearchView({
      model: this.model,
      el:    this.$('.openlayers')
    });

    this.searchResultsView = new SearchPageView({
      model: this.model,
      el:    this.$('.results')
    });

    this.drawingControlView = new DrawingControlView({
      model: this.model,
      el:    this.$('.drawing-control')
    });

    return this.facetsPanelView = new FacetsPanelView({
      model: this.model,
      el:    this.$('.facet-filter')
    });
  }
});
 });
