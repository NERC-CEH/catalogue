import ObjectInputView from './ObjectInputView'
import template from '../templates/keyword.tpl'
import _ from 'underscore/underscore-node'

export default ObjectInputView.extend({ template: _.template(template) })
