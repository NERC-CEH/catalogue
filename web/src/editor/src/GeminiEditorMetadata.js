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
      this.set('hasOnlineServiceAgreement', false)
    }
  },

  validate (attrs) {
    const errors = EditorMetadata.prototype.validate.call(this, attrs) ?? []

    const { hasOnlineServiceAgreement, resourceType } = attrs

    // Plain data (SingleObjectView stores it so); {} when none was selected, as the server drops
    // Keyword's empty strings
    const newType = resourceType?.value

    if (hasOnlineServiceAgreement && !mayHaveOnlineServiceAgreement.includes(newType)) {
      errors.push('Invalid resource type to have an online service agreement')
    }

    if (errors.length) {
      return errors
    }
  }
})
