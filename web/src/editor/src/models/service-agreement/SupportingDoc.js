import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    content: 'Collection/generation methods\nNature and Units of recorded values\nQuality control\nDetails of data structure\nExperimental design/Sampling regime\nFieldwork and laboratory instrumentation\nCalibration steps and values\nAnalytical methods\nAny other information useful to the interpretation of the data'
  }
})
