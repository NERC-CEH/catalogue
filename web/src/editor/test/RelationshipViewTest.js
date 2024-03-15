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
})
