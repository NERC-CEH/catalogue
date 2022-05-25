import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/MapReprojection.tpl'

export default ObjectInputView.extend({

  initialize () {
    this.template = _.template(template)
  },

  events: _.extend({}, ObjectInputView.prototype.events,
    { 'click button.remove': 'delete' }),

  delete () { return this.model.collection.remove(this.model) }
})
