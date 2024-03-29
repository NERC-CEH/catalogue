import Backbone from 'backbone'
import template from '../templates/LinkDocument'

export default Backbone.View.extend({

  events: {
    'click button': 'selected'
  },

  render () {
    this.template = template
    this.$el.html(this.template(this.model.attributes))
    return this
  },

  selected () {
    this.model.trigger('selected', this.model.get('identifier'))
  }
})
