import _ from 'underscore'

import ObjectInputView from './ObjectInputView'
import template from '../templates/DeimsSite.tpl'

export default ObjectInputView.extend({

  initialize () {
    this.template = _.template(template)
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

        $.getJSON(query, data => response(_.map(data, d => ({
          value: d.title,
          label: d.title,
          id: d.id,
          url: d.url
        }))))
      }
    })

    this.$('.autocomplete').on('autocompleteselect', (event, ui) => {
      this.model.set('id', ui.item.id)
      this.$('.id').val(ui.item.id)
      this.model.set('title', ui.item.label)
      this.$('.title').val(ui.item.label)
      this.model.set('url', ui.item.url)
      this.$('.url').val(ui.item.url)
    })
  }
})
