define [
  'underscore'
  'backbone'
  'cs!views/upload/simple/FileView'
  'tpl!templates/upload/simple/Files.tpl'
], (_, Backbone, FileView, template) -> Backbone.View.extend

  events:
    'click .delete-selected': 'deleteSelected'
    'click .select-all': 'selectAll'

  template: template

  initialize: (options) ->
    @files = options.files
    @messages = options.messages

    @$fileList = @$('#files-list')
    @$tools = @$('#files-tools')

    @listenTo(@files, 'add', @addOne)
    @listenTo(@files, 'reset', @addAll)

    @render()

  addOne: (file) ->
    view = new FileView({model: file})
    @$fileList.append(view.render().el)

  addAll: ->
    @$fileList.empty()
    @files.each(@addOne, @)

  deleteSelected: ->
    toDelete = @files.where({'toDelete': true})
    if confirm("Delete #{toDelete.length} files?")
      options = {
        success: (model) =>
          @messages.add(new Backbone.Model({message: "Deleted: #{model.get('name')}", type: 'info'}))
        error: (model, response) =>
          @messages.add(new Backbone.Model(response))
      }
      _.invoke(toDelete, 'destroy', options)

  selectAll: ->
    @files.invoke('set', 'toDelete', true)

  render: ->
    @$tools.html(@template())
    @
