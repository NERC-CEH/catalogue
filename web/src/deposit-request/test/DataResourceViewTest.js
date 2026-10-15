import { afterEach, beforeEach, describe, expect, it } from 'vitest'
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

  // The follow-up questions a dataset asks, and what it reports missing (dri-one #473)
  describe('as a dataset is described', () => {
    function choose (view, name, value) {
      view.$(`[data-name="${name}"]`).val(value).trigger('change')
    }

    it('asks for the type only when it is "Other", and forgets it otherwise', () => {
      choose(first, 'resourceType', 'Other')
      expect(first.$('[data-name="resourceTypeOther"]').css('display')).not.toBe('none')
      choose(first, 'resourceTypeOther', 'Field notebooks')

      choose(first, 'resourceType', 'Images')

      expect(first.$('[data-name="resourceTypeOther"]').css('display')).toBe('none')
      expect(first.model.get('resourceTypeOther')).toBe('')
    })

    it('asks whether it could be recreated only for model output or "Other"', () => {
      choose(first, 'resourceType', 'Model output')
      expect(first.$('.easilyRecreated').css('display')).not.toBe('none')

      choose(first, 'resourceType', 'Monitoring data')

      expect(first.$('.easilyRecreated').css('display')).toBe('none')
      expect(first.model.get('easilyRecreated')).toBe('')
    })

    it('asks for the format only when it is "Other", and forgets it otherwise', () => {
      choose(first, 'resourceFormat', 'Other')
      expect(first.$('[data-name="resourceFormatOther"]').css('display')).not.toBe('none')

      choose(first, 'resourceFormat', 'NetCDF')

      expect(first.$('[data-name="resourceFormatOther"]').css('display')).toBe('none')
      expect(first.model.get('resourceFormatOther')).toBe('')
    })

    it('marks what is missing, and checks again as answers change', () => {
      const view = row('c5', {})

      const focus = view.validate()

      expect(focus.attr('data-name')).toBe('title')
      expect(view.$('[data-name="title"]').hasClass('is-invalid')).toBe(true)
      expect(view.$('[data-name="title"]').siblings('.invalid-feedback').text()).toBe('Working title is required')

      choose(view, 'title', 'Rainfall')

      expect(view.$('[data-name="title"]').hasClass('is-invalid')).toBe(false)
      expect(view.$('[data-name="description"]').hasClass('is-invalid')).toBe(true)
    })
  })

  describe('validation', () => {
    const valid = {
      title: 'Rainfall',
      description: 'Daily rainfall',
      resourceType: 'Monitoring data',
      resourceFormat: 'NetCDF',
      size: '1 GB',
      largeData: 'false'
    }
    const errorNames = attributes => (new DataResource(attributes).validate() || []).map(error => error.name)

    it('accepts a complete dataset', () => {
      expect(errorNames(valid)).toEqual([])
    })

    it('requires the "other" type and format when they are chosen', () => {
      expect(errorNames({ ...valid, resourceType: 'Other', easilyRecreated: 'true' })).toEqual(['resourceTypeOther'])
      expect(errorNames({ ...valid, resourceFormat: 'Other' })).toEqual(['resourceFormatOther'])
    })

    it('requires an answer on recreating model output', () => {
      expect(errorNames({ ...valid, resourceType: 'Model output' })).toEqual(['easilyRecreated'])
    })
  })
})
