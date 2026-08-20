import $ from 'jquery'
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

/**
 * The rows are addressed by their DOM data-index attribute, so a delete that shortens this.array
 * without renumbering the surviving rows leaves the two out of step and every later edit splices at
 * the wrong slot - NERC-CEH/dri-one#298.
 */
describe('ParentStringView mid-list delete', () => {
  const buildEnabled = (attrs, modelAttribute) => {
    const model = new Backbone.Model(attrs)
    const view = new ParentStringView({ model, modelAttribute })
    return { model, view }
  }

  const indices = view => view.$('input').map((i, el) => $(el).attr('data-index')).get()
  const rowIds = view => view.$('.existing > div').map((i, el) => el.id).get()
  const rowWithValue = (view, value) => view.$('input').filter((i, el) => el.value === value)

  const deleteRow = (view, value) => {
    const $row = rowWithValue(view, value).closest('div').parent()
    $row.find('button.remove').trigger('click')
  }

  const editRow = (view, from, to) => {
    const $input = rowWithValue(view, from)
    $input.val(to)
    $input.trigger('change')
  }

  it('renumbers the surviving rows so data-index matches the array position', () => {
    const { view } = buildEnabled({ alternateTitles: ['aaa', 'bbb', 'ccc'] }, 'alternateTitles')

    deleteRow(view, 'bbb')

    expect(view.array).toEqual(['aaa', 'ccc'])
    expect(indices(view)).toEqual(['0', '1'])
  })

  it('keeps the row ids contiguous so no two rows share one', () => {
    const { view } = buildEnabled({ alternateTitles: ['aaa', 'bbb', 'ccc'] }, 'alternateTitles')

    deleteRow(view, 'bbb')

    expect(rowIds(view)).toEqual(['inputalternateTitles0', 'inputalternateTitles1'])
  })

  it('updates a later row instead of duplicating it after a mid-list delete', () => {
    const { model, view } = buildEnabled({ alternateTitles: ['aaa', 'bbb', 'ccc'] }, 'alternateTitles')
    deleteRow(view, 'bbb')

    editRow(view, 'ccc', 'ccc-edited')

    expect(view.array).toEqual(['aaa', 'ccc-edited'])
    expect(model.get('alternateTitles')).toEqual(['aaa', 'ccc-edited'])
  })

  it('gives an added row an index of its own after a mid-list delete', () => {
    const { view } = buildEnabled({ alternateTitles: ['aaa', 'bbb', 'ccc'] }, 'alternateTitles')
    deleteRow(view, 'bbb')

    view.$('button.add').trigger('click')

    expect(view.array).toEqual(['aaa', 'ccc', ''])
    expect(indices(view)).toEqual(['0', '1', '2'])
    expect(rowIds(view)).toEqual(['inputalternateTitles0', 'inputalternateTitles1', 'inputalternateTitles2'])
  })

  it('writes the added row to the model without disturbing the survivors', () => {
    const { model, view } = buildEnabled({ inputs: ['aaa', 'bbb', 'ccc'] }, 'inputs')
    deleteRow(view, 'bbb')
    view.$('button.add').trigger('click')

    editRow(view, '', 'ddd')

    expect(model.get('inputs')).toEqual(['aaa', 'ccc', 'ddd'])
  })

  /** The added row used to reuse a survivor's slot, so editing either one overwrote the other. */
  it('does not let an added row overwrite the survivor it was numbered after', () => {
    const { model, view } = buildEnabled({ outputs: ['aaa', 'bbb', 'ccc'] }, 'outputs')
    deleteRow(view, 'bbb')
    view.$('button.add').trigger('click')

    editRow(view, 'ccc', 'ccc-edited')

    expect(view.array).toEqual(['aaa', 'ccc-edited', ''])
    expect(model.get('outputs')).toEqual(['aaa', 'ccc-edited'])
  })

  it('survives deleting the first row of three', () => {
    const { model, view } = buildEnabled({ users: ['aaa', 'bbb', 'ccc'] }, 'users')

    deleteRow(view, 'aaa')
    editRow(view, 'ccc', 'ccc-edited')

    expect(model.get('users')).toEqual(['bbb', 'ccc-edited'])
  })

  it('survives two mid-list deletes in a row', () => {
    const { model, view } = buildEnabled({ packages: ['aaa', 'bbb', 'ccc', 'ddd'] }, 'packages')

    deleteRow(view, 'bbb')
    deleteRow(view, 'ccc')
    editRow(view, 'ddd', 'ddd-edited')

    expect(view.array).toEqual(['aaa', 'ddd-edited'])
    expect(model.get('packages')).toEqual(['aaa', 'ddd-edited'])
  })
})
