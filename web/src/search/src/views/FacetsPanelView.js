import Backbone from 'backbone'
import panelTpl from '../templates/facetsPanelTemplate'
import resultsTpl from '../templates/facetResultsTemplate'
import deparam from 'deparam.js'
import 'select2'

export default Backbone.View.extend({

  initialize () {
    this.listenTo(this.model, 'results-sync', this.render)
  },

  createFacetSearch (model) {
    let closeDropdown = false
    const searchFacets = this.$('.search-facet')
    if (!searchFacets.data('select2')) {
      searchFacets.select2({
        theme: 'bootstrap-5',
        width: '100%',
        selectionCssClass: 'select2--large',
        dropdownCssClass: 'select2--large',
        templateSelection: e => { return e.text.split(' (')[0] }
      })
      this.$('.select2-selection__choice').removeAttr('title')

      searchFacets.on('select2:select', e => {
        this.handleMultiSelect(e.params.data.id, model)
      })

      searchFacets.on('select2:unselect', e => {
        closeDropdown = true
        this.handleMultiSelect(e.params.data.id, model)
      })

      searchFacets.on('select2:opening', e => {
        if (closeDropdown) {
          e.preventDefault()
          closeDropdown = false
        }
      })
    }
  },

  destroyFacetSearch () {
    const searchFacets = this.$('.search-facet')
    if (searchFacets.data('select2')) {
      searchFacets.select2('close')
      searchFacets.select2('destroy')
      searchFacets.off('select2:select')
      searchFacets.off('select2:unselect')
      searchFacets.off('select2:opening')
    }
  },

  handleMultiSelect (url, model) {
    const query = url.split('?')[1]
    if (query) {
      model.setState(deparam(query, true))
    } else {
      model.setState({})
    }
    return false
  },

  /*
     * Render the facet results panel as long as we have some results currently set.
     *
     * The template panelTpl requires a sub template which renders each facet
     * results set.
     */
  render () {
    this.destroyFacetSearch()

    const facets = this.model.getResults().attributes.facets
    this.$el.html(panelTpl({
      facets,
      template: resultsTpl
    }))

    this.createFacetSearch(this.model)
    return this
  }
})
