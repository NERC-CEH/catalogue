import $ from 'jquery'
import DataResource from '../src/model/DataResource'
import DataResourceView from '../src/view/DataResourceView'

/**
 * Each dataset in a deposit request has its own radio buttons, but their ids were fixed, so every
 * label pointed at the first dataset's radio: clicking "Yes" on dataset 2 changed dataset 1's answer,
 * which was then sent to Jira (NERC-CEH/dri-one#461).
 */
describe('DataResourceView', () => {
  let first, second

  function row (index, attributes) {
    const model = new DataResource(attributes)
    const view = new DataResourceView({ model, index })
    $(document.body).append(view.el)
    return view
  }

  beforeEach(() => {
    first = row('c1', { resourceType: 'Model output' })
    second = row('c2', { resourceType: 'Model output' })
  })

  afterEach(() => $(document.body).empty())

  function clickLabel (view, name, answer) {
    const radio = view.$(`[data-name="${name}"][value="${answer}"]`)[0]
    view.$(`label[for="${radio.id}"]`)[0].click()
  }

  it('changes only its own dataset when a label is clicked', () => {
    clickLabel(second, 'easilyRecreated', 'true')
    clickLabel(second, 'largeData', 'false')

    expect(second.model.get('easilyRecreated')).toBe('true')
    expect(second.model.get('largeData')).toBe('false')
    expect(first.model.get('easilyRecreated')).not.toBe('true')
    expect(first.model.get('largeData')).not.toBe('false')
  })

  it('gives every radio an id of its own', () => {
    const ids = first.$('input[type=radio]').add(second.$('input[type=radio]')).map((i, el) => el.id).get()
    expect(new Set(ids).size).toBe(ids.length)
  })

  it('shows a saved "other" resource type', () => {
    const view = row('c3', { resourceType: 'Other', resourceTypeOther: 'Field notebooks' })

    expect(view.$('[data-name="resourceTypeOther"]').val()).toBe('Field notebooks')
  })

  it('shows a saved answer, as the form stores them (strings)', () => {
    const view = row('c4', { resourceType: 'Model output', easilyRecreated: 'false', largeData: 'true' })

    expect(view.$('[data-name="easilyRecreated"][value="false"]').prop('checked')).toBe(true)
    expect(view.$('[data-name="largeData"][value="true"]').prop('checked')).toBe(true)
  })
})
