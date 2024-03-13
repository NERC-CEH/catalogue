 import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import template from '../templates/searchPage'

export default Backbone.View.extend({

  initialize () {
    this.template = template
    this.readModelFromHTML()

    this.listenTo(this.model, 'cleared:results', this.clear)
    this.listenTo(this.model, 'results-sync', this.render)
    this.listenTo(this.model, 'results-change:selected', this.updateSelected)
  },

  /*
     * A page of search results may already be loaded in to the html. We can read
     * that html and populate the model. If there is no html this method SHOULD NOT
     * trigger a change on the model.
     */
  readModelFromHTML () {
    this.model.getResults().set({
      numFound: this.$('#num-records').val(),
      results: _.map(this.$('.result'), r => ({
        identifier: $(r).attr('id'),
        title: $('.result__title', r).text(),
        description: $('.result__description', r).text(),
        locations: $(r).attr('data-location').split('|')
      }))
    })
  },

  /*
     * Event listener for when the selected id has changed on the search page model.
     * Highlight that search result, with the selected class
     */
  updateSelected () {
    this.$('.result').removeClass('selected')
    this.$(`#${this.model.getResults().get('selected')}`)
      .addClass('selected')
  },

  /*
     * Clear the dom of any content
     */
  clear () {
    this.$el.empty()
  },

  /*
     * Draw in the new content
     */
  render () {
    this.$el.html(this.template(this.model.getResults().attributes))
    const $relatedSearches = this.$('.results__related_searches')
    const relatedSearches = this.model.getResults().get('relatedSearches')
    if (relatedSearches != null) {
      if (relatedSearches.length > 0) {
        $relatedSearches.append('<h3>Related Searches</h3>')
      }

      relatedSearches.forEach(function (relatedSearch, index) {
        let prefix
        if (index > 0) {
          prefix = ', '
        } else {
          prefix = ''
        }
        $relatedSearches.append(
                    `${prefix}<a href="${relatedSearch.href}">${relatedSearch.title}</a>`
        )
      })
    }

    return this
  }
})
