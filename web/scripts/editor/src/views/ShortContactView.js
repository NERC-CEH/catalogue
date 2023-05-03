import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/ShortContact.tpl'

export default ObjectInputView.extend({ template: _.template(template) })
