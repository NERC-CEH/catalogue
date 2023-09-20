import ParentStringView from './ParentStringView'
import childTemplate from '../templates/MultiStringTextbox'

export default ParentStringView.extend({

  initialize (options) {
    this.template = childTemplate
    return ParentStringView.prototype.initialize.call(this, options)
  }
})
