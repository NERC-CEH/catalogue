import Backbone from 'backbone'

export default Backbone.Model.extend({
  defaults: {
    title: '',
    description: '',
    resourceType: '',
    resourceTypeOther: '',
    easilyRecreated: '',
    resourceFormat: '',
    resourceFormatOther: '',
    size: ''
  },

  validate (attrs) {
    if (!attrs) {
      attrs = this.attributes
    }
    const errors = []

    if (attrs.title === '') errors.push({ name: 'title', message: 'Working title is required' })
    if (attrs.description === '') errors.push({ name: 'description', message: 'Brief description is required' })
    if (attrs.resourceType === '') {
      errors.push({ name: 'resourceType', message: 'Resource type is required.' })
    } else {
      if (attrs.resourceType === 'Other' && attrs.resourceTypeOther === '') {
        errors.push({ name: 'resourceTypeOther', message: 'Resource type not specified.' })
      }
      if ((attrs.resourceType === 'Other' || attrs.resourceType === 'Model output') && attrs.easilyRecreated === '') {
        errors.push({ name: 'easilyRecreated', message: 'Selection is required.' })
      }
    }
    if (attrs.resourceFormat === '') {
      errors.push({ name: 'resourceFormat', message: 'Resource format is required.' })
    } else {
      if (attrs.resourceFormat === 'Other' && attrs.resourceFormatOther === '') {
        errors.push({ name: 'resourceFormatOther', message: 'Resource format not specified.' })
      }
    }
    if (attrs.size === '') errors.push({ name: 'size', message: 'Size is required.' })

    return errors.length > 0 ? errors : undefined
  }
})
