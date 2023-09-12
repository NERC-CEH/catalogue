import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/Author.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  initialize () {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.call(this)
  },

  modify (event) {
    ObjectInputView.prototype.modify.call(this, event)
    this.model.set('role', 'author')
  }
})
