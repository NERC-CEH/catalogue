import _ from 'underscore'
import Backbone from 'backbone'
import { FileView } from './FileView'
import template from './Files.tpl'

export var FileListView = Backbone.View.extend({

  events: {
    'click .delete-selected': 'deleteSelected',
    'click .select-all': 'selectAll'
  },

  initialize (options) {
    this.files = options.files
    this.messages = options.messages

    this.$fileList = this.$('#files-list')
    this.$tools = this.$('#files-tools')

    this.listenTo(this.files, 'add', this.addOne)
    this.listenTo(this.files, 'reset', this.addAll)

    return this.render()
  },

  addOne (file) {
    const view = new FileView({ model: file })
    this.$fileList.append(view.render().el)
  },

  addAll () {
    this.$fileList.empty()
    this.files.each(this.addOne, this)
  },

  deleteSelected () {
    const toDelete = this.files.where({ toDelete: true })
    if (confirm(`Delete ${toDelete.length} files?`)) {
      const options = {
        success: model => {
          this.messages.add(new Backbone.Model({ message: `Deleted: ${model.get('name')}`, type: 'info' }))
        },
        error: (model, response) => {
          this.messages.add(new Backbone.Model(response))
        }
      }
      _.invoke(toDelete, 'destroy', options)
    }
  },

  selectAll () {
    this.files.invoke('set', 'toDelete', true)
  },

  render () {
    this.$tools.html(_.template(template))
    return this
  }
})
