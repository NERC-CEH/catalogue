import Backbone from 'backbone'
import template from '../templates/DrawingControl.tpl'
import _ from 'underscore'

export default Backbone.View.extend({
  events: {
    'click #drawing-toggle': 'toggleDrawing',
    'click #spatial-op-dropdown': 'open'
  },

  /*
  This is the drawing control view. If not bounding box is currently set, it
  will show a drawing tool so that a boundbox area can be selected
  */
  initialize () {
    this.template = _.template(template)
    this.render()
    this.listenTo(this.model, 'results-change', this.render)
    this.listenTo(this.model, 'change:drawing', this.updateDrawingToggle)
  },

  /*
  Ensure that the panel is open
  */
  open () { this.model.set({ mapsearch: true }) },

  /*
  Toggle the drawing mode of the model and ensure app is in map search mode
  */
  toggleDrawing () {
    this.model.set({
      drawing: !this.model.get('drawing'),
      mapsearch: true
    })
  },

  /*
  Update the state of the drawing toggle button. Add and remove the active
  class depending on the drawing state of the model
  */
  updateDrawingToggle () {
    const toggle = this.model.get('drawing') ? 'addClass' : 'removeClass'
    this.$('button')[toggle]('active')
  },

  render () {
    return this.$el.html(this.template({
      url: this.model.getResults().get('url'),
      withoutBbox: this.model.getResults().get('withoutBbox'),
      withinBbox: this.model.getResults().get('withinBbox'),
      intersectingBbox: this.model.getResults().get('intersectingBbox')
    })
    )
  }
})
