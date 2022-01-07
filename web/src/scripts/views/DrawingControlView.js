// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'tpl!templates/DrawingControl.tpl'
], function($, Backbone, template) { return Backbone.View.extend({
  events: {
    "click #drawing-toggle":       "toggleDrawing",
    "click #spatial-op-dropdown":  "open"
  },

  /*
  This is the drawing control view. If not bounding box is currently set, it 
  will show a drawing tool so that a boundbox area can be selected
  */
  initialize() {
    (this.render)();
    this.listenTo(this.model, 'results-change', this.render);
    return this.listenTo(this.model, 'change:drawing', this.updateDrawingToggle);
  },

  /*
  Ensure that the panel is open
  */
  open() { return this.model.set({mapsearch:true}); },

  /*
  Toggle the drawing mode of the model and ensure app is in map search mode
  */
  toggleDrawing() { 
    return this.model.set({
      drawing:   !this.model.get('drawing'),
      mapsearch: true
    });
  },

  /*
  Update the state of the drawing toggle button. Add and remove the active
  class depending on the drawing state of the model
  */
  updateDrawingToggle() { 
    const toggle = this.model.get('drawing') ? 'addClass' : 'removeClass';
    return this.$('button')[toggle]('active');
  },

  render() {  return this.$el.html(template({
    url:              this.model.getResults().get('url'),
    withoutBbox:      this.model.getResults().get('withoutBbox'),
    withinBbox:       this.model.getResults().get('withinBbox'),
    intersectingBbox: this.model.getResults().get('intersectingBbox')
  })
  ); }
});
 });