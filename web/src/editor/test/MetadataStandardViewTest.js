import $ from 'jquery'
import Backbone from 'backbone'
import MetadataStandardView from '../src/views/MetadataStandardView'

describe('MetadataStandardView', () => {
  it('keeps the date that is typed', () => {
    const model = new Backbone.Model({ name: 'ISO 19115', date: '2003-05-01' })
    const view = new MetadataStandardView({ model, index: 'c1' })

    const date = view.$('#metadataStandardc1Date')
    date.val('2014-04-01')
    date[0].dispatchEvent(new Event('input'))

    expect(model.get('date')).toBe('2014-04-01')
    expect($.contains(view.el, date[0])).toBe(true)
  })
})
