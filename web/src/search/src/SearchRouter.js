import $ from 'jquery'
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
    if (typeof window.history.replaceState !== 'undefined') {
      let url = this.appUrl
      const param = $.param(this.model.getState())
      if (param) {
        url = url.concat('?', param)
      }
      window.history.replaceState({ catalogueSearch: 'update search url' }, '', url)
    } else {
      this.navigate($.param(this.model.getState(), true), { replace: true })
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
