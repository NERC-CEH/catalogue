import ParentStringView from './ParentStringView'
import _ from 'underscore'
import childTemplate from '../templates/MultiStringTextbox.tpl'

export default ParentStringView.extend({

  initialize (options) {
    this.template = _.template(childTemplate)
    return ParentStringView.prototype.initialize.call(this, options)
  }
})
