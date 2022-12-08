import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/ProvenanceLink.tpl'

export default ObjectInputView.extend({

  initialize () {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.apply(this)
    const catalogue = $('html').data('catalogue')

    this.$('.autocompleteFrom').autocomplete({
      minLength: 2,
      source (request, response) {
        let query
        const term = request.term.trim()
        if (_.isEmpty(term)) {
          query = `/${catalogue}/documents`
        } else {
          query = `/${catalogue}/documents?term=${request.term}`
        }

        $.getJSON(query, data => response(_.map(data.results, d => ({
          value: d.title,
          label: d.title,
          identifier: d.identifier,
          type: d.resourceType
        }))))
      }
    })

    this.$('.autocompleteTo').autocomplete({
      minLength: 2,
      source (request, response) {
        let query
        const term = request.term.trim()
        if (_.isEmpty(term)) {
          query = `/${catalogue}/documents`
        } else {
          query = `/${catalogue}/documents?term=${request.term}`
        }

        $.getJSON(query, data => response(_.map(data.results, d => ({
          value: d.title,
          label: d.title,
          identifier: d.identifier,
          type: d.resourceType
        }))))
      }
    })

    this.$('.autocompleteFrom').on('autocompleteselect', (event, ui) => {
      this.model.set('identifierFrom', ui.item.identifier)
      this.$('.identifierFrom').val(ui.item.identifier)
      this.model.set('hrefFrom', 'https://catalogue.ceh.ac.uk/id/' + ui.item.identifier)
      this.$('.hrefFrom').val('https://catalogue.ceh.ac.uk/id/' + ui.item.identifier)
      this.model.set('titleFrom', ui.item.label)
      this.$('.titleFrom').val(ui.item.label)
    })

    this.$('.autocompleteTo').on('autocompleteselect', (event, ui) => {
      this.model.set('identifierTo', ui.item.identifier)
      this.$('.identifierTo').val(ui.item.identifier)
      this.model.set('hrefTo', 'https://catalogue.ceh.ac.uk/id/' + ui.item.identifier)
      this.$('.hrefTo').val('https://catalogue.ceh.ac.uk/id/' + ui.item.identifier)
      this.model.set('titleTo', ui.item.label)
      this.$('.titleTo').val(ui.item.label)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.rel').val(this.model.get('rel'))
    return this
  }
})
