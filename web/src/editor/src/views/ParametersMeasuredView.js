import template from '../templates/ParametersMeasured'
import ObjectInputViewForObjects from './ObjectInputViewForObjects'

export default ObjectInputViewForObjects.extend({
    initialize () {
        this.template = template
    },
    render () {
        if (!Array.from(this.data).includes('name')) {
            this.data.name = {}
        }

        if (!Array.from(this.data).includes('unitOfMeasure')) {
            this.data.unitOfMeasure = {}
        }

        ObjectInputViewForObjects.prototype.render.apply(this)
        return this
    }
})
