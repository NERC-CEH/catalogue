import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/ObservedProperty'

export default ObjectInputView.extend({

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
  },

  modify (event) {
    const $target = $(event.target)
    const name = $target.data('name')
    const value = $target.val()

    if (_.contains(['maximum', 'minimum', 'maxLength', 'minLength', 'unique'], name)) {
      let constraints = _.clone(this.model.get('constraints'))
      if (value) {
        constraints[name] = value
        this.model.set('constraints', constraints)
      } else {
        constraints = _.omit(constraints, name)
        this.model.set('constraints', constraints)
      }
    } else {
      if (value) {
        this.model.set(name, value)
      } else {
        this.model.unset(name)
      }
    }
  }
})
