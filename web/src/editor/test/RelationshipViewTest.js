import RelationshipView from '../src/views/RelationshipView.js'
import { EditorMetadata } from '../src'
import $ from 'jquery'
import 'jquery-ui/ui/widgets/autocomplete'

describe('Test RelationshipView', function () {
  let model = null
  let view = null
  const options = [{ value: 'http://purl.org/dc/terms/relation', label: 'Relationship' }]

  beforeEach(function () {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: '' })
    view = new RelationshipView({ model, options })
    spyOn($, 'getJSON').and.callFake((url) => {
      return {
        title: 'title',
        id: 'uid-123',
        type: 'type'
      }
    })
  })

  it('test render', () => {
    // when
    view.render()
    // then
    expect(view.$('.relationshipSearch')).toBeDefined()
  })

  it('should set relationshipSearch to d-none if target exists', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: 'target' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect(view.$('.relationshipSearch').hasClass('d-none')).toBeTrue()
  })

  it('should disable relationshipList if target exists', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: 'target' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect(view.$('.relationshipList').prop('disabled')).toBeTrue()
  })

  it('should set relationshipRecord to d-none if target does not exist', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: '' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect(view.$('.relationshipRecord').hasClass('d-none')).toBeTrue()
  })

  it('should enable autocomplete when relationship selected', async () => {
    await view.render()

    view.$('.relationshipList')
      .val('http://purl.org/dc/terms/relation')
      .trigger('change')

    expect(view.$('.autocomplete').prop('disabled')).toBeFalse()
  })

  it('should do correct http call for uid', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: 'exampleUid' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect($.getJSON).toHaveBeenCalledWith('/documents/exampleUid')
  })

  it('should do correct http call for uri', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: 'http://exampleUri' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect($.getJSON).toHaveBeenCalledWith('http://exampleUri')
  })

  describe('relationship-specific search filters', function () {
    const outputOptions = [{ value: 'http://purl.org/cerif/frapo/hasOutput', label: 'Has output' }]

    async function queryFor (relation, opts) {
      const m = new EditorMetadata({ value: relation, target: '' })
      const v = new RelationshipView({ model: m, options: opts })
      // The autocomplete is bound during initialize and render() replaces the
      // input, so capture the source callback before rendering.
      const source = v.$('.autocomplete').autocomplete('option', 'source')
      await v.render()
      v.$('.relationshipList').val(relation)
      await source({ term: 'rainfall' }, () => {})
      return $.getJSON.calls.mostRecent().args[0]
    }

    it('restricts hasOutput to output-bearing resource types', async () => {
      const query = await queryFor('http://purl.org/cerif/frapo/hasOutput', outputOptions)
      expect(query).toContain('recordType%3A(')
      expect(query).toContain('Dataset')
      expect(query).toContain('Map')
      expect(query).toContain('Model')
      expect(query).toContain('Software')
    })
  })
})
