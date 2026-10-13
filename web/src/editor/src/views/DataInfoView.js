import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/DataInfo'

// Shared by two editors whose server classes name the parameter differently: NERC model use has
// `name`, CEH model application has `variableName` (dri-one #455)
export default ObjectInputView.extend({
  template,

  initialize (options) {
    ObjectInputView.prototype.initialize.call(this, _.extend({ nameAttribute: 'name' }, options))
  }
})
