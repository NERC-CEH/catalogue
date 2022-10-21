import _ from 'underscore'

import Backbone from 'backbone'
import template from '../templates/Message.tpl'

export default Backbone.View.extend({
  el: '#message-panel',

  defaultMessage: 'A requested resource failed to load.',

  events: {
    'click span.fas': 'dismissMessage'
  },

  initialize () {
    this.template = _.template(template)
    this.listenTo(this.model, 'error', this.appendError)
    this.listenTo(this.model, 'info', this.appendInfo)
  },

  /*
    Handle the error event.
    */
  appendError (model, res) {
    let message
    this.$el.show()

    if (_.isString(model)) {
      message = model
    }

    this.$el.append(this.template({
      type: 'danger',
      message: message || __guard__(res != null ? res.responseJSON : undefined, x => x.message) || this.defaultMessage
    })
    )
  },

  /*
    Handle the info event.
    */
  appendInfo (message) {
    this.$el.show()

    this.$el.append(this.template({
      type: 'info',
      message
    })
    )
  },

  dismissMessage (evt) {
    $(evt.target).parent('.alert').remove()
    if (this.$el.children().length === 0) { this.$el.hide() }
  }
})
function __guard__ (value, transform) {
  return (typeof value !== 'undefined' && value !== null) ? transform(value) : undefined
}
