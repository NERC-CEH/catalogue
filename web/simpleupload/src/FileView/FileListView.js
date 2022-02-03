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
  'cs!views/upload/simple/FileView',
  'tpl!templates/upload/simple/Files.tpl'
], function (_, Backbone, FileView, template) {
  return Backbone.View.extend({

    events: {
      'click .delete-selected': 'deleteSelected',
      'click .select-all': 'selectAll'
    },

    template,

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
      return this.$fileList.append(view.render().el)
    },

    addAll () {
      this.$fileList.empty()
      return this.files.each(this.addOne, this)
    },

    deleteSelected () {
      const toDelete = this.files.where({ toDelete: true })
      if (confirm(`Delete ${toDelete.length} files?`)) {
        const options = {
          success: model => {
            return this.messages.add(new Backbone.Model({ message: `Deleted: ${model.get('name')}`, type: 'info' }))
          },
          error: (model, response) => {
            return this.messages.add(new Backbone.Model(response))
          }
        }
        return _.invoke(toDelete, 'destroy', options)
      }
    },

    selectAll () {
      return this.files.invoke('set', 'toDelete', true)
    },

    render () {
      this.$tools.html(this.template())
      return this
    }
  })
})
