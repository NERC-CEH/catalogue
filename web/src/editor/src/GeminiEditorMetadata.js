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

    // resourceType is a plain object if it's unchanged, possibly {} when none was selected (the
    // server drops Keyword's empty strings), or a Backbone model once the editor has set it
    const newType = typeof resourceType?.get === 'function' ? resourceType.get('value') : resourceType?.value

    if (hasOnlineServiceAgreement && !mayHaveOnlineServiceAgreement.includes(newType)) {
      errors.push('Invalid resource type to have an online service agreement')
    }

    if (errors.length) {
      return errors
    }
  }
})
