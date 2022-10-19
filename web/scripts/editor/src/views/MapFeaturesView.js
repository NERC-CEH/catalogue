import ObjectInputView from './ObjectInputView'
import MapStyleSelectorView from './MapStyleSelectorView'
import template from '../templates/MapFeatures.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.call(this, options)

    // eslint-disable-next-line no-unused-vars
    const model = this.model.getRelated('style')
    new MapStyleSelectorView({
      el: this.$('.style-selector'),
      model: this.model.getRelated('style')
    })
  }
})
