import Backbone from 'backbone'
import template from './File.tpl'

export var FileView = Backbone.View.extend({

  tagName: 'li',

  template,

  events: {
    'change input': 'select'
  },

  initialize () {
    this.listenTo(this.model, 'sync', this.remove)
    return this.listenTo(this.model, 'change', this.render)
  },

  select () {
    const previous = this.model.get('toDelete')
    return this.model.set('toDelete', !previous)
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  }
})
