import ObjectInputViewForObjects from './ObjectInputViewForObjects'
import template from '../templates/ObservationCapability'

export default ObjectInputViewForObjects.extend({
    initialize () {
        this.template = template
    },
    render () {
        if (!Array.from(this.data).includes('observedPropertyName')) {
            this.data.observedPropertyName = {}
        }

        if (!Array.from(this.data).includes('observedPropertyUnitOfMeasure')) {
            this.data.observedPropertyUnitOfMeasure = {}
        }

        if (!Array.from(this.data).includes('procedureName')) {
            this.data.procedureName = {}
        }

        ObjectInputViewForObjects.prototype.render.apply(this)
        return this
    }
})
