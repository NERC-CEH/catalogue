import $ from 'jquery'
import { StudyAreaView } from '../src/View'

describe('Test StudyAreaView', function () {
  let el
  let view
  const template = '<div id="studyarea-map"><span content="{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Point&quot;,&quot;coordinates&quot;:[-2.64501,54.5261]}}" dataType="geoJson"/></div>'

  beforeEach(function () {
    el = $(template).appendTo($('body'))
    view = new StudyAreaView({
      el
    })
  })

  afterEach(() => el.remove())

  it('renders', () => expect(view).toBeDefined())
})
