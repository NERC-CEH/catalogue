define [
  'underscore'
  'backbone'
  'cs!views/upload/simple/MessageView'
  'tpl!templates/upload/simple/Messages.tpl'
], (_, Backbone, MessageView, template) -> Backbone.View.extend

  events:
    'click .clear-all': 'clearAll'

  template: template

  initialize: (options) ->
    @messages = options.messages

    @$tools = @$('#messages-tools')

    @$messageList = @$('#messages-list')

    @listenTo(@messages, 'add', @addMessage)

    @render()

  addMessage: (message) ->
    view = new MessageView({model: message})
    @$messageList.append(view.render().el)

  clearAll: ->
    _.invoke(@messages.toArray(), 'destroy')

  render: ->
    @$tools.html(@template())
    @