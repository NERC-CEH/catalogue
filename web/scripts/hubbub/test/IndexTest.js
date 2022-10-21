
import init from '../src/index'

describe('index.js', function () {
  it('initializes view', function () {
    // given
    const id = 'dacaa5ae-67bd-40d8-ad7f-f37ece3ff09a'
    $(document.body)
      .html(`
        <div id="document-upload" data-guid="${id}">
            <div class="document-upload"></div>
        </div>
      `)

    // when
    const view = init()

    // then
    expect(view.model.get('id')).toEqual(id)
  })
})
