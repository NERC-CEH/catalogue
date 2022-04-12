import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/Author.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  modify (event) {
    this.template = _.template(template)
    ObjectInputView.prototype.modify.call(this, event)
    return this.model.set('role', 'author')
  }
})
