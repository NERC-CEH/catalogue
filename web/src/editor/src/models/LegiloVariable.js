import Backbone from 'backbone'

export default Backbone.Model.extend({
  defaults: {
    name: '',
    longName: '',
    units: '',
    meaning: '',
    confidence: 0.0
  }
})
