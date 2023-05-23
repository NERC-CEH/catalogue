import _ from 'underscore'
import template from '../templates/OtherConstraint.tpl'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({ template: _.template(template) })
