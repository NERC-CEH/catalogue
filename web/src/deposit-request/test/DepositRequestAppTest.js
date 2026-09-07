import DepositRequestApp from '../src/model/DepositRequestApp'

describe('DepositRequestApp.validate', () => {
  let model

  const validAttrs = () => ({
    name: 'Ada Lovelace',
    email: 'ada@example.com',
    affiliation: 'UKCEH',
    scienceArea: 'External',
    checklist1: true,
    checklist2: true,
    checklist3: true,
    checklist4: true,
    funder: 'NERC',
    funderOther: '',
    eidcRemit: 'yes',
    alternativeData: 'no',
    hasSupportingDocs: 'no',
    isSupportingDocsReady: 'no',
    replaceExisting: 'no',
    relatedToExisting: 'no',
    dataResources: [{}]
  })

  const errorNames = (errors) => (errors || []).map(e => e.name)

  beforeEach(() => {
    model = new DepositRequestApp()
  })

  it('returns undefined when every required field is valid', () => {
    expect(model.validate(validAttrs())).toBeUndefined()
  })

  it('flags every required field on an empty default model', () => {
    const errors = model.validate(model.defaults)
    expect(errorNames(errors)).toEqual(jasmine.arrayContaining([
      'name', 'email', 'affiliation',
      'checklist1', 'checklist2', 'checklist3', 'checklist4',
      'funder', 'dataResources'
    ]))
  })

  it('rejects a malformed email address', () => {
    const attrs = { ...validAttrs(), email: 'not-an-email' }
    expect(errorNames(model.validate(attrs))).toEqual(['email'])
  })

  it('requires funderOther when funder is "Other"', () => {
    const attrs = { ...validAttrs(), funder: 'Other', funderOther: '' }
    expect(errorNames(model.validate(attrs))).toEqual(['funderOther'])
  })

  it('requires at least one data resource', () => {
    const attrs = { ...validAttrs(), dataResources: [] }
    expect(errorNames(model.validate(attrs))).toEqual(['dataResources'])
  })
})
