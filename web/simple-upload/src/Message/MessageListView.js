import _ from 'underscore'
import Backbone from 'backbone'
import { MessageView } from './MessageView'
import template from './Messages.tpl'

export var MessageListView = Backbone.View.extend({

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
