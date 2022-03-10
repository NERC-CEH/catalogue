import Backbone from 'backbone'
import template from '../templates/LinkDocument.tpl'
import _ from 'underscore/underscore-node'

export default Backbone.View.extend({

  events: {
    'click button': 'selected'
  },

  render () {
    this.template = _.template(template)
    this.$el.html(template(this.model.attributes))
    return this
  },

  selected (event) {
    return this.model.trigger('selected', this.model.get('identifier'))
  }
})
