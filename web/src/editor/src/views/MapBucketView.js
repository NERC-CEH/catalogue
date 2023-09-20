import ObjectInputView from './ObjectInputView'
import MapStyleSelectorView from './MapStyleSelectorView'
import template from '../templates/MapBucket'
import { MapDataSource } from '../models'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)

    this.model = new MapDataSource(this.model.attributes)
    // eslint-disable-next-line no-unused-vars
    const view = new MapStyleSelectorView({
      el: this.$('.style-selector'),
      model: this.model.getRelated('style'),
      disabled: options.disabled
    })
  }
})
