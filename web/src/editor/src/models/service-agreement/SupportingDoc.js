import Backbone from 'backbone'

export default Backbone.Model.extend({

  // Remember to update the corresponding strings in the Java code at
  // serviceagreement.SupportingDoc.contentTypeToText if you change
  // any of these

  mandatoryContentTypes: {
    generationMethods: 'Collection/generation methods',
    natureUnits: 'Nature and Units of recorded values',
    qc: 'Quality control',
    dataStructure: 'Details of data structure'
  },

  conditionalContentTypes: {
    experimentalDesign: 'Experimental design/Sampling regime',
    instrumentation: 'Fieldwork and laboratory instrumentation',
    calibrationSteps: 'Calibration steps and values',
    analyticalMethods: 'Analytical methods',
    other: 'Any other information useful to the interpretation of the data'
  }
})
