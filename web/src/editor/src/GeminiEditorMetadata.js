import EditorMetadata from './EditorMetadata'

const mayHaveOnlineServiceAgreement = [
  'dataset',
  'nonGeographicDataset',
  'application'
]

export default EditorMetadata.extend({

  initialize (data, options, title) {
    EditorMetadata.prototype.initialize.call(this, data, options, title)
    if (this.isNew()) {
      this.set('hasOnlineServiceAgreement', true)
    }
  },

  validate (attrs) {
    const errors = EditorMetadata.prototype.validate.call(this, attrs) ?? []

    const { hasOnlineServiceAgreement, resourceType } = attrs

    // resourceType is a plain object if it's unchanged
    // or a Backbone model if it has been updated
    // we need to handle both cases
    const newType = resourceType?.value ?? resourceType?.get('value')

    if (hasOnlineServiceAgreement && !mayHaveOnlineServiceAgreement.includes(newType)) {
      errors.push('Invalid resource type to have an online service agreement')
    }

    if (errors.length) {
      return errors
    }
  }
})
