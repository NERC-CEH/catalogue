import Backbone from 'backbone'
import deparam from 'deparam.js'

export default Backbone.Router.extend({
  routes: {
    '*data': 'updateModel'
  },

  initialize (options) {
    this.model = options.model

    // If there is no hash component, we can use the query string to update the
    // model to represent the state of the document which is already loaded
    if (!options.location.hash) {
      this.updateModel(options.location.search.substring(1), { silent: true })
    }

    this.model.on('change', () => this.updateRoute())
  },

  /*
    Gets the state of the model and turns it into a query state string which this
    router will be able to parse and process at a later time
    */
  updateRoute () {
    const queryString = $.param(this.model.getState(), true)
    this.navigate(queryString, { replace: true })
  },

  /*
    Updates the model given the specified state object. Options can be passed to
    avoid unnessersary triggering of events
    */
  updateModel (state, options) {
    if (state) { this.model.setState(deparam(state, true), options) }
  }
})
