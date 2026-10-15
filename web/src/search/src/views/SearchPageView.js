import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import template from '../templates/searchPage'

// The search to share, without its paging. Splitting on & dropped the base URL along with page when
// page came first (dri-one #460)
export function shareUrl (href) {
  const url = new URL(href)
  url.searchParams.delete('page')
  url.searchParams.delete('rows')
  return url.toString()
}

// Some mail programs refuse a mailto: link much longer than this
export const MAILTO_LIMIT = 2000
// A blank line between entries; a mailto: body breaks lines as CRLF (RFC 6068)
const BREAK = '\r\n\r\n'

/*
 * The email that shares a search: the catalogue and today's date in the subject, the search's
 * address, then each result on the page by title and address, in order. Results that would take the
 * link past MAILTO_LIMIT are left out and counted instead, the search's address showing them all.
 */
export function shareEmail ({ catalogue, href, results = [], date = new Date(), limit = MAILTO_LIMIT }) {
  const day = date.toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' })
  const subject = `${catalogue} search results, ${day}`
  const origin = new URL(href).origin
  const head = `Search: ${shareUrl(href)}`
  const entries = results.map((result, i) => {
    // A server-rendered title is read from the page, line breaks and all
    const title = String(result.title || result.identifier).replace(/\s+/g, ' ').trim()
    return `${i + 1}. ${title}\r\n   ${origin}/documents/${encodeURIComponent(result.identifier)}`
  })
  const mailto = body => `mailto:?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`

  let shown = entries.length
  let link = mailto([head, ...entries].join(BREAK))
  while (link.length > limit && shown > 0) {
    shown--
    const more = `…and ${entries.length - shown} more: see the search link`
    link = mailto([head, ...entries.slice(0, shown), more].join(BREAK))
  }
  return link
}

export default Backbone.View.extend({

  events: {
    'change .sort-search': 'handleSortChange',
    // Its location is picked out on the map
    'mouseenter .result': 'hover',
    'mouseleave .result': 'unhover'
  },

  initialize () {
    this.template = template
    this.readModelFromHTML()

    this.listenTo(this.model, 'cleared:results', this.clear)
    this.listenTo(this.model, 'results-sync', this.render)
    this.listenTo(this.model, 'results-change:selected', this.updateSelected)
  },

  handleSortChange (e) {
    const value = e.currentTarget.value
    let sortField = null
    let order = null

    if (value) {
      const parts = value.split('-')
      sortField = parts[0]
      if (parts.length > 1) {
        order = parts[1]
      }
    }
    this.model.set({ sortField, order })
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
    const selected = this.$('.result').filter((i, result) => result.id === this.model.getResults().get('selected'))
    selected.addClass('selected')
    // Picked on the map, perhaps further down the page
    selected[0]?.scrollIntoView({ block: 'nearest' })
  },

  hover (event) {
    this.model.getResults()?.set('hovered', event.currentTarget.id)
  },

  unhover () {
    this.model.getResults()?.unset('hovered')
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
    const modelAttributes = this.model.getResults().attributes

    modelAttributes.sortField = this.model.get('sortField') || null
    modelAttributes.order = this.model.get('order') || 'asc'
    modelAttributes.catalogueId = this.$el.closest('[data-catalogue]').data('catalogue')

    this.$el.html(this.template(modelAttributes))
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
        // Titles are vocabulary labels from a third-party source: set them as text
        $relatedSearches.append(prefix, $('<a>').attr('href', relatedSearch.href).text(relatedSearch.title))
      })
    }

    this.initSearchShare(this.$('#searchShare'))

    return this
  },

  initSearchShare (el) {
    el.click(() => this.openMail(shareEmail({
      catalogue: $('meta[name="og.site_name"]').attr('content') || String($('html').data('catalogue') || '').toUpperCase(),
      href: window.location.href,
      results: this.model.getResults()?.get('results')
    })))
  },

  // Opens the visitor's mail program
  openMail (link) {
    window.location.href = link
  }
})
