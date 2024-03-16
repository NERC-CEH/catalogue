import RelationshipView from '../src/views/RelationshipView.js'
import { EditorMetadata } from '../src'
import 'jquery-ui/ui/widgets/autocomplete'

describe('Test RelationshipView', function () {
  let model = null
  let view = null
  const options = [{ value: 'option1', label: 'Relationship' }]

  beforeEach(function () {
    model = new EditorMetadata({ target: 'target' })
    view = new RelationshipView({ model, options })
    spyOn($, 'getJSON').and.callFake((url) => {
       return {
         title: 'title',
         id: 'uid-123',
         type: 'type',
       }
    });
  })

  it('test render', () => {
    // when
    view.render()
    // then
    expect(view.$('.relationshipSearch')).toBeDefined()
  })

  it('should set relationshipSearch to hidden if target exists', () => {
    model = new EditorMetadata({ target: 'target' })
    view = new RelationshipView({ model, options })
    view.render()
    expect(view.$('.relationshipSearch').hasClass('hidden')).toBeTrue()
  })

  it('should set relationshipRecord to hidden if target does not exist', () => {
    model = new EditorMetadata({ target: '' })
    view = new RelationshipView({ model, options })
    view.render()
    expect(view.$('.relationshipRecord').hasClass('hidden')).toBeTrue()
  })
  
  it('should do correct http call for uid', () => {
      model = new EditorMetadata({ target: 'exampleUid' })
      view = new RelationshipView({ model, options })
      expect($.getJSON).toHaveBeenCalledWith('/documents/exampleUid')
  })

  it('should do correct http call for uri', () => {
      model = new EditorMetadata({ target: 'http://exampleUri' })
      view = new RelationshipView({ model, options })
      expect($.getJSON).toHaveBeenCalledWith('http://exampleUri')
  })
})
