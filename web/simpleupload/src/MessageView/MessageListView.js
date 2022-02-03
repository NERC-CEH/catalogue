/* eslint-disable
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone',
  'cs!views/upload/simple/MessageView',
  'tpl!templates/upload/simple/Messages.tpl'
], function (_, Backbone, MessageView, template) {
  return Backbone.View.extend({

    events: {
      'click .clear-all': 'clearAll'
    },

    template,

    initialize (options) {
      this.messages = options.messages

      this.$tools = this.$('#messages-tools')

      this.$messageList = this.$('#messages-list')

      this.listenTo(this.messages, 'add', this.addMessage)

      return this.render()
    },

    addMessage (message) {
      const view = new MessageView({ model: message })
      return this.$messageList.append(view.render().el)
    },

    clearAll () {
      return _.invoke(this.messages.toArray(), 'destroy')
    },

    render () {
      this.$tools.html(this.template())
      return this
    }
  })
})
