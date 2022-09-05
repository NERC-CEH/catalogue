import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/MapReprojection.tpl'

export default ObjectInputView.extend({

  events: _.extend({}, ObjectInputView.prototype.events,
    { 'click button.remove': 'delete' }),

  initialize () {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.apply(this)
  }
})
