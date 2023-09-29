import Backbone from 'backbone'
import template from '../templates/KeywordCheckbox'

export default Backbone.View.extend({

  className: 'vocab',

  events: {
    'change input': 'select'
  },

  initialize () {
    this.template = template
    this.listenTo(this.model, 'sync', this.remove)
    this.listenTo(this.model, 'change', this.render)
  },

  select () {
    const previous = this.model.get('toSearch')
    this.model.set('toSearch', !previous)
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  }
})
