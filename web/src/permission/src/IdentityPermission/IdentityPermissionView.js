import $ from 'jquery'
import Backbone from 'backbone'

export default Backbone.View.extend({

  initialize (options) {
    if (typeof options !== 'undefined' && Object.hasOwn(options, 'template')) {
      this.template = options.template
    }
  },

  tagName: 'tr',

  events: {
    'click button': 'removePermission',
    'click [type="checkbox"]': 'update'
  },

  removePermission () {
    this.model.parent.removePermission(this.model)
  },

  update (event) {
    const permission = $(event.target).data('permission')
    this.model.set(permission, !this.model.get(permission))
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  }
})
