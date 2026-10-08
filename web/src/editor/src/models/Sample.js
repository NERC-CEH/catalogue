import Backbone from 'backbone'
import Geometry from '../geometryMap/Geometry'

export default Backbone.Model.extend({
  defaults: function () {
    return {
      sampleID: '',
      sampleSomething: '',
      sampleLocation: new Geometry()
    }
  },

  initialize () {
    let geometry = this.get('sampleLocation')

    if (!(geometry instanceof Geometry)) {
      geometry = new Geometry(geometry || {})
      this.set('sampleLocation', geometry, { silent: true })
    }
  }
})