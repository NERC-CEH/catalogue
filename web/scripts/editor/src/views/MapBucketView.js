import ObjectInputView from './ObjectInputView'
import MapStyleSelectorView from './MapStyleSelectorView'
import template from '../templates/MapBucket.tpl'
import _ from 'underscore'
import { NestedModel } from '../models'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.call(this, options)

    const model = new NestedModel(this.model.attributes)
    const that = this
    // eslint-disable-next-line no-unused-vars
    const view = new MapStyleSelectorView({
      el: that.$('.style-selector'),
      model: model.getRelated('style'),
      disabled: options.disabled
    })
  }
})
