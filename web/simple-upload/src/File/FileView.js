import Backbone from 'backbone'
import template from './File.tpl'
import _ from 'underscore'

export default Backbone.View.extend({

  tagName: 'li',

  events: {
    'change input': 'select'
  },

  initialize () {
    this.template = _.template(template)
    this.listenTo(this.model, 'sync', this.remove)
    this.listenTo(this.model, 'change', this.render)
  },

  select () {
    const previous = this.model.get('toDelete')
    this.model.set('toDelete', !previous)
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  }
})
