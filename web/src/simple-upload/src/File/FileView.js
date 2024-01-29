import _ from 'underscore'
import Backbone from 'backbone'

const template = _.template(`
<label><%= name %>
    <input type="checkbox" <% if (toDelete) { %> checked <% } %>/>
</label>
`)

export default Backbone.View.extend({

  tagName: 'li',

  events: {
    'change input': 'select'
  },

  initialize () {
    this.template = template
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
