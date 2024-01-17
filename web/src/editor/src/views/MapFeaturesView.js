import ObjectInputView from './ObjectInputView'
import MapStyleSelectorView from './MapStyleSelectorView'
import template from '../templates/MapFeatures'

export default ObjectInputView.extend({

    initialize (options) {
        this.template = template
        ObjectInputView.prototype.initialize.call(this, options)

        /* eslint no-new: "off" */
        new MapStyleSelectorView({
            el: this.$('.style-selector'),
            model: this.model.getRelated('style')
        })
    }
})
