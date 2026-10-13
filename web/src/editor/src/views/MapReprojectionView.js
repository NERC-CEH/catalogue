import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/MapReprojection'

export default ObjectInputView.extend({

  events: _.extend({}, ObjectInputView.prototype.events,
    { 'click button.remove': 'delete' }),

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this, arguments)
  },

  // These rows have no ChildView wrapper, so this button is the only way to remove one
  delete () {
    this.model.collection.remove(this.model)
  }
})
