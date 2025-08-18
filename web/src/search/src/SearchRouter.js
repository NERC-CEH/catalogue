import Backbone from 'backbone'
import deparam from 'deparam.js'

export default Backbone.Router.extend({
  routes: {
    '?*data': 'updateModel'
  },

  initialize (options) {
    this.model = options.model
    this.appUrl = options.location.href.split('#')[0].split('?')[0]

    // If there is no hash component, we can use the query string to update the
    // model to represent the state of the document which is already loaded
    if (!options.location.hash) {
      this.updateModel(options.location.search.substring(1), { silent: true })
    }

    this.model.on('change', () => this.updateRoute())
  },

  /*
     * Gets the state of the model and turns it into a query state string which this
     * router will be able to parse and process at a later time
     */
  updateRoute () {
    const state = this.model.getState()
    const params = []

    const facetMap = {}

    if (state.facet) {
      const facets = Array.isArray(state.facet) ? state.facet : [state.facet]

      facets.forEach(entry => {
        const [field, value] = entry.split('|')
        if (!facetMap[field]) {
          facetMap[field] = []
        }
        facetMap[field].push(value)
      })

      Object.keys(facetMap).forEach(field => {
        const values = facetMap[field]
        const encoded = values.length === 1
          ? `${field}|${values[0]}`
          : `${field}|(${values.join(' OR ')})`
        params.push('facet=' + encodeURIComponent(encoded))
      })
    }

    Object.keys(state).forEach(key => {
      if (key === 'facet') return
      const value = state[key]
      if (Array.isArray(value)) {
        value.forEach(v => params.push(`${encodeURIComponent(key)}=${encodeURIComponent(v)}`))
      } else {
        params.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
      }
    })

    const url = this.appUrl + (params.length ? '?' + params.join('&') : '')

    if (window.history.replaceState) {
      window.history.replaceState({ catalogueSearch: 'update search url' }, '', url)
    } else {
      this.navigate(params.join('&'), { replace: true })
    }
  },

  /*
     * Updates the model given the specified state object. Options can be passed to
     * avoid unnecessary triggering of events
     */
  updateModel (state, options) {
    if (state) { this.model.setState(deparam(state, true), options) }
  }
})
