import ObjectInputView from './ObjectInputView'
import { Geometry, GeometryView } from '../geometryMap'
import template from '../templates/Sample'

export default ObjectInputView.extend({
  template,

  render () {
    ObjectInputView.prototype.render.apply(this, arguments)

    const sampleLocation = this.model.get('sampleLocation') || {}

    this.geometry = new Geometry(sampleLocation)

    this.geometryView = new GeometryView({
      model: this.geometry
    })

    this.listenTo(this.geometry, 'change', () => {
      this.model.set('sampleLocation', this.geometry.toJSON())
    })

    const container = this.$('.geometry-container')[0]

    if (container) {
      container.appendChild(this.geometryView.el)

      this.resizeObserver = new ResizeObserver(() => {
        if (
          container.offsetWidth > 0 &&
          container.offsetHeight > 0 &&
          this.geometryView &&
          this.geometryView.map
        ) {
          this.geometryView.map.invalidateSize()
        }
      })

      this.resizeObserver.observe(container)
    }

    return this
  },
  
  remove () {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect()
      this.resizeObserver = null
    }

    if (this.geometryView) {
      this.geometryView.remove()
      this.geometryView = null
    }

    this.geometry = null

    return ObjectInputView.prototype.remove.apply(this, arguments)
  }
})