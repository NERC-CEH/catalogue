import ObjectInputView from './ObjectInputView'
import MapStyleSelectorView from './MapStyleSelectorView'
import template from '../templates/MapValue.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.call(this, options)

    const view = new MapStyleSelectorView({
      el: this.$('.style-selector'),
      model: this.model.getRelated('style'),
      disabled: options.disabled
    })
  }
})
