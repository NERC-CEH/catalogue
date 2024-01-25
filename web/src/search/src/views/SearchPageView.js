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

    this.padResults() // Change the padding of the results page

    // Ensure @ refers to this in the findSelected method. This means that we
    // can:
    //   - Pass @findSelected directly to the jquery.on method
    //   - Unbind @findSelected when the view is removed
    _.bindAll(this, 'padResults')
    $(window).on('resize', this.padResults)
  },

  /*
     * Since we update the selected result based upon the scroll position, we need
     * to be able to scroll all the way to select the last result. This method will
     * ensure that the margin of the results view allows for such.
     */
  padResults () {
    if (this.$('.result').length) {
      // Where the top result should start
      const topPosition = $('.navbar').height() + $('.search-form').height()
      const onScreen = this.$('.result').last().nextAll().addBack() // Elements to show
      const onScreenHeight = _.reduce(onScreen,
        (height, e) => height + $(e).outerHeight(true),
        topPosition)

      this.$el.css({ marginBottom: $(window).height() - onScreenHeight })
    }
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
    this.padResults()
    return this
  } // Pad the results pane
})
