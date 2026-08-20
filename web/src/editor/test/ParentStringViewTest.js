import Backbone from 'backbone'
import ParentStringView from '../src/views/ParentStringView'

/**
 * ParentStringView is the string-array counterpart of ParentView, driving alternateTitles, inputs,
 * outputs, packages, temporalResolution and users. Its add button pushes an empty string and syncs
 * straight to the model, so a row added and never typed into used to be persisted as "" - the same
 * defect as the blank keyword objects in NERC-CEH/dri-one#297, one type down.
 */
const build = (attrs, modelAttribute) => {
  const model = new Backbone.Model(attrs)
  const view = new ParentStringView({ model, modelAttribute, disabled: 'disabled' })
  return { model, view }
}

const stubEvent = target => ({ preventDefault () {}, target, currentTarget: target })

describe('ParentStringView empty rows', () => {
  it('does not write a newly added empty row into the model', () => {
    const { model, view } = build({ alternateTitles: [] }, 'alternateTitles')

    view.addChild(stubEvent())

    expect(model.get('alternateTitles')).toEqual([])
  })

  it('still shows the added row in the form so it can be typed into', () => {
    const { view } = build({ alternateTitles: [] }, 'alternateTitles')

    view.addChild(stubEvent())

    expect(view.array.length).toBe(1)
    expect(view.$('input[data-index="0"]').length).toBe(1)
  })

  it('writes the row to the model as soon as it is typed into', () => {
    const { model, view } = build({ alternateTitles: [] }, 'alternateTitles')
    view.addChild(stubEvent())

    const input = view.$('input[data-index="0"]')
    input.val('Upper Severn gauge')
    view.modify(stubEvent(input[0]))

    expect(model.get('alternateTitles')).toEqual(['Upper Severn gauge'])
  })

  it('keeps populated rows and drops only the empty one', () => {
    const { model, view } = build({ alternateTitles: ['Severn', 'Wye'] }, 'alternateTitles')

    view.addChild(stubEvent())

    expect(model.get('alternateTitles')).toEqual(['Severn', 'Wye'])
  })

  it('drops a whitespace-only row', () => {
    const { model, view } = build({ users: ['CEH'] }, 'users')

    view.array.push('   ')
    view.updateModel()

    expect(model.get('users')).toEqual(['CEH'])
  })

  /** "0" is a legitimate value, not emptiness - it must survive the filter. */
  it('keeps a row whose value is the string zero', () => {
    const { model, view } = build({ temporalResolution: [] }, 'temporalResolution')

    view.array.push('0')
    view.updateModel()

    expect(model.get('temporalResolution')).toEqual(['0'])
  })

  /** A record loaded with a blank already stored is cleaned on the next edit. */
  it('drops a blank that was already stored when the field is next updated', () => {
    const { model, view } = build({ alternateTitles: ['Severn', ''] }, 'alternateTitles')

    view.updateModel()

    expect(model.get('alternateTitles')).toEqual(['Severn'])
  })

  /**
   * this.array stays whole so it keeps lining up with the DOM data-index attributes that modify and
   * removeChild splice against. Only the copy handed to the model is filtered.
   */
  it('leaves the working array untouched so DOM indices still line up', () => {
    const { view } = build({ alternateTitles: ['Severn'] }, 'alternateTitles')

    view.addChild(stubEvent())

    expect(view.array).toEqual(['Severn', ''])
  })
})
