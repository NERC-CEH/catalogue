import { NestedModel } from '../../../editor/src/models/NestedModel'
import DataResource from './DataResource'

export default NestedModel.extend({
  defaults: {
    name: '',
    email: '',
    affiliation: '',
    isAgreed: false,
    funder: '',
    funderOther: '',
    fundingRef: '',
    eidcRemit: '',
    alternativeData: '',
    hasSupportingDocs: '',
    isSupportingDocsReady: '',
    replaceExisting: '',
    relatedToExisting: '',
    dataResources: [],
    additionalInfo: ''
  },

  getDataResources () {
    return this.getRelatedCollection('dataResources', DataResource)
  },

  validate (attrs) {
    if (!attrs) {
      attrs = this.attributes
    }
    const errors = []

    if (attrs.name === '') errors.push({ name: 'name', message: 'Name is required.' })
    if (attrs.email === '') {
      errors.push({ name: 'email', message: 'Email is required.' })
    } else {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(attrs.email)) {
        errors.push({ name: 'email', message: 'Invalid email.' })
      }
    }
    if (attrs.affiliation === '') errors.push({ name: 'affiliation', message: 'Affiliation is required.' })
    if (!attrs.isAgreed) errors.push({ name: 'isAgreed', message: 'You must agree to the terms and conditions.' })
    if (attrs.funder === '') {
      errors.push({ name: 'funder', message: 'Funder is required.' })
    } else {
      if (attrs.funder === 'Other' && attrs.funderOther === '') {
        errors.push({ name: 'funderOther', message: 'Funder(s) not specified.' })
      }
    }
    if (attrs.hasSupportingDocs === '') errors.push({ name: 'hasSupportingDocs', message: 'Selection is required.' })
    if (attrs.replaceExisting === '') errors.push({ name: 'replaceExisting', message: 'Selection is required.' })
    if (attrs.relatedToExisting === '') errors.push({ name: 'relatedToExisting', message: 'Selection is required.' })

    if (attrs.dataResources.length === 0) errors.push({ name: 'dataResources', message: 'At least one data resource is required.' })

    return errors.length > 0 ? errors : undefined
  },

  url () { return window.location.pathname }
})
