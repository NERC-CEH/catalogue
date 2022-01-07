// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'tpl!templates/LayerControls.tpl',
  'tpl!templates/LayerInfo.tpl',
  'jquery-ui/slider',
  'bootstrap'
], function(Backbone, controlsTemplate, infoTemplate) { return Backbone.View.extend({

  events: {
    "change input.visibility": "updateVisibility",
    "slide .slider"(evt, ui) { return this.model.set('opacity', ui.value); },
    "click .info" : "toggleLayerInfo"
  },

  initialize() {
    (this.render)();

    this.listenTo(this.model, 'change:visibility', this.updateToggle);
    this.listenTo(this.model, 'change:opacity', this.updateOpacity);
    return this.listenTo(this.model, 'change:infoVisible', this.updateInfoVisibility);
  },

  toggleLayerInfo() {
    return this.model.setInfoVisibility(!this.model.get('infoVisible'));
  },

  updateInfoVisibility() { 
    const visibility = this.model.get('infoVisible') ? 'show' : 'hide';
    return this.$('.info').popover(visibility);
  },

  /*
  Updates the toggle based upon the state of the visibility property
  */
  updateToggle() {
    return this.$("input.visibility").prop("checked", this.model.get("visibility"));
  },

  /*
  Updates the visibility property on the model based upon the state of the of 
  the ui toggle 
  */
  updateVisibility() {
    const toggle = this.$('input.visibility').is(':checked');
    return this.model.set('visibility', toggle);
  },

  /*
  Syncs the value for opacity set in the model to the value the slider is
  currently showing
  */
  updateOpacity() {
    this.opacity = this.model.getOpacity();
    return this.opacitySlider.slider("value", this.opacity);
  },

  render() {
    this.$el.html(controlsTemplate(this.model.toJSON()));
    this.opacitySlider = this.$('.slider').slider({max: 1, step: 0.01});
    
    this.infoPopover = this.$('.info').popover({
      placement: 'right',
      content:    infoTemplate(this.model), // Generate some info content
      trigger:    'manual',            // We will handle the popover in backbone
      html:       true,
      animation:  false,
      viewport:   '#mapviewer',
      container:  '#mapviewer'
    });

    (this.updateOpacity)(); //Ensure that the opacity value is set correctly
    return (this.updateInfoVisibility)();
  }
});
 });