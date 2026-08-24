import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'

import template from '../templates/Funding'
import { createOrgAutocomplete } from '../utils'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    this.index = options.index

    ObjectInputView.prototype.initialize.apply(this)
    createOrgAutocomplete('.orgAutocomplete', this, 'funderIdentifier')
  },

  render () {
    this.$el.html(this.template({ data: _.extend({}, this.model.attributes, { index: this.index }) }))
    return this
  }
})
