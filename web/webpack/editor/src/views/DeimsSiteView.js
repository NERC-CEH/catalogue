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
  'jquery',
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/DeimsSite.tpl',
  'jquery-ui/autocomplete'
], function (_, $, ObjectInputView, template) {
  return ObjectInputView.extend({

    template,

    initialize () {
      ObjectInputView.prototype.initialize.apply(this)

      this.$('.autocomplete').autocomplete({
        minLength: 2,
        source (request, response) {
          let query
          const term = request.term.trim()
          if (_.isEmpty(term)) {
            query = '/vocabulary/deims'
          } else {
            query = `/vocabulary/deims?query=${request.term}`
          }

          return $.getJSON(query, data => response(_.map(data, d => ({
            value: d.title,
            label: d.title,
            id: d.id,
            url: d.url
          }))))
        }
      })

      return this.$('.autocomplete').on('autocompleteselect', (event, ui) => {
        this.model.set('id', ui.item.id)
        this.$('.id').val(ui.item.id)
        this.model.set('title', ui.item.label)
        this.$('.title').val(ui.item.label)
        this.model.set('url', ui.item.url)
        return this.$('.url').val(ui.item.url)
      })
    }
  })
})
