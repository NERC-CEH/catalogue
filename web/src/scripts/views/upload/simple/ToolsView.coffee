define [
  'underscore'
  'backbone'
  'tpl!templates/upload/simple/Tools.tpl'
], (_, Backbone, template) -> Backbone.View.extend

  events:
    'click .delete-selected': 'deleteSelected'
    'click .select-all': 'selectAll'

  template: template

  initialize: (options) ->
    @files = options.files
    @messages = options.messages
    @render()

  render: ->
    @$el.html(@template())
    @

  deleteSelected: ->
    toDelete = @files.where({'toDelete': true})
    if confirm("Delete #{toDelete.length} files?")
      options = {
        success: (model, response) =>
          @messages.add(new Backbone.Model({message: response.message, type: 'info'})) if response.message?
        error: (model, response) =>
          @messages.add(new Backbone.Model({message: response.message, type: 'error'})) if response.message?
      }
      _.invoke(toDelete, 'destroy', options)

  selectAll: ->
    @files.invoke('set', 'toDelete', true)
