import $ from 'jquery'
import DepositRequestApp from '../src/model/DepositRequestApp'
import DepositRequestFormView from '../src/view/DepositRequestFormView'
import { fakeServer } from '../../test/support'

// The EIDC deposit request form: what it shows as it's filled in, how it reports what's missing, and
// what it does with the server's answer (dri-one #473)
describe('DepositRequestFormView', () => {
  let view

  const complete = () => ({
    name: 'Ada Lovelace',
    email: 'ada@example.org',
    affiliation: 'UKCEH',
    checklist1: true,
    checklist2: true,
    checklist3: true,
    checklist4: true,
    funder: 'NERC',
    eidcRemit: 'yes',
    alternativeData: 'no',
    hasSupportingDocs: 'no',
    isSupportingDocsReady: 'no',
    replaceExisting: 'no',
    relatedToExisting: 'no',
    dataResources: [{
      title: 'Rainfall',
      description: 'Daily rainfall',
      resourceType: 'Dataset',
      resourceFormat: 'CSV',
      size: '1 GB',
      largeData: 'false'
    }]
  })

  function form (attributes) {
    view = new DepositRequestFormView({ el: $('<div>').appendTo(document.body), model: new DepositRequestApp(attributes) })
    return view
  }

  function change (selector, value) {
    const $field = view.$(selector)
    if ($field.attr('type') === 'checkbox') $field.prop('checked', value)
    else $field.val(value)
    $field.trigger('change')
  }

  beforeEach(() => {
    fakeServer.install()
    spyOn(Element.prototype, 'scrollIntoView')
  })

  afterEach(() => {
    fakeServer.uninstall()
    $(document.body).empty()
  })

  describe('as it is filled in', () => {
    it('keeps each answer on the request', () => {
      form()
      change('[data-name="name"]', '  Ada Lovelace  ')
      change('[data-name="checklist1"]', true)

      expect(view.model.get('name')).toBe('Ada Lovelace')
      expect(view.model.get('checklist1')).toBeTrue()
    })

    it('asks which funders only when the funder is "Other", and forgets them otherwise', () => {
      form()
      change('[data-name="funder"]', 'Other')
      expect(view.$('[data-name="funderOther"]').css('display')).not.toBe('none')
      change('[data-name="funderOther"]', 'Wellcome')

      change('[data-name="funder"]', 'NERC')

      expect(view.$('[data-name="funderOther"]').css('display')).toBe('none')
      expect(view.$('[data-name="funderOther"]').val()).toBe('')
      expect(view.model.get('funderOther')).toBe('')
    })

    it('asks for a science area only of UKCEH staff', () => {
      form()
      change('[data-name="email"]', 'someone@ceh.ac.uk')
      expect(view.$('#scienceAreaContainer').hasClass('d-none')).toBeFalse()

      change('[data-name="email"]', 'someone@example.org')

      expect(view.$('#scienceAreaContainer').hasClass('d-none')).toBeTrue()
      expect(view.model.get('scienceArea')).toBeNull()
    })

    it('shows the science area already, for a UKCEH address it opens with', () => {
      form({ email: 'someone@ceh.ac.uk' })

      expect(view.$('#scienceAreaContainer').hasClass('d-none')).toBeFalse()
    })

    it('shows the answers it opens with', () => {
      form({ name: 'Ada Lovelace', funder: 'Other', funderOther: 'Wellcome', eidcRemit: 'yes', checklist2: true })

      expect(view.$('[data-name="name"]').val()).toBe('Ada Lovelace')
      expect(view.$('[data-name="funder"]').val()).toBe('Other')
      expect(view.$('[data-name="funderOther"]').val()).toBe('Wellcome')
      expect(view.$('[data-name="checklist2"]').prop('checked')).toBeTrue()
    })
  })

  describe('datasets', () => {
    it('adds a dataset, and offers another', () => {
      form()
      view.$('.add-resource').trigger('click')

      expect(view.model.getDataResources().length).toBe(1)
      expect(view.$('.resource-list').children().length).toBe(1)
      expect(view.$('.add-resource').text()).toBe('Add another dataset')
    })

    it('goes back to offering a first dataset when the last is removed', () => {
      form()
      view.$('.add-resource').trigger('click')

      view.$('.resource-list button.editor-button-remove').trigger('click')

      expect(view.model.getDataResources().length).toBe(0)
      expect(view.$('.add-resource').text()).toBe('Add a dataset')
    })
  })

  describe('when submitted incomplete', () => {
    it('marks what is missing, scrolls to the first, and sends nothing', () => {
      form()
      view.$('.btn-submit').trigger('click')

      expect(view.$('[data-name="name"]').hasClass('is-invalid')).toBeTrue()
      expect(view.$('[data-name="name"]').siblings('.invalid-feedback').text()).toBe('Name is required')
      expect(view.$('[data-name="dataResources"]').hasClass('is-invalid')).toBeTrue()
      expect(Element.prototype.scrollIntoView).toHaveBeenCalled()
      expect(fakeServer.requestCount()).toBe(0)
    })

    it('checks again as each answer is corrected', () => {
      form()
      view.$('.btn-submit').trigger('click')

      change('[data-name="name"]', 'Ada Lovelace')

      expect(view.$('[data-name="name"]').hasClass('is-invalid')).toBeFalse()
      expect(view.$('[data-name="email"]').hasClass('is-invalid')).toBeTrue()
    })

    it('marks what is missing from a dataset', () => {
      form()
      view.$('.add-resource').trigger('click')
      view.$('.btn-submit').trigger('click')

      expect(view.$('.resource-list [data-name="title"]').hasClass('is-invalid')).toBeTrue()
    })
  })

  describe('when submitted complete', () => {
    function submit () {
      form(complete())
      view.$('.btn-submit').trigger('click')
      return fakeServer.lastRequest()
    }

    it('sends the request as JSON, and disables the button meanwhile', () => {
      const request = submit()

      expect(request.method).toBe('POST')
      expect(request.url).toBe(window.location.pathname)
      expect(view.$('.btn-submit').prop('disabled')).toBeTrue()
    })

    it('goes where the server says, once it is accepted', () => {
      const request = submit()
      spyOn(view, 'navigateTo')

      request.respondWith({ status: 201, contentType: 'text/plain', responseText: '', responseHeaders: { Location: '/deposit-request/success' } })

      expect(view.navigateTo).toHaveBeenCalledWith('/deposit-request/success')
    })

    it('shows the server\'s message when there is nowhere to go', () => {
      const request = submit()

      request.respondWith({ status: 200, contentType: 'text/plain', responseText: 'Thank you' })

      expect(view.$('#message-box').text()).toBe('Thank you')
      expect(view.$('.btn-submit').prop('disabled')).toBeFalse()
    })

    it('says it was submitted when the server says nothing', () => {
      const request = submit()

      request.respondWith({ status: 200, contentType: 'text/plain', responseText: '' })

      expect(view.$('#message-box').text()).toBe('Submission successful.')
    })

    it('shows the server\'s error, and lets the request be sent again', () => {
      const request = submit()

      request.respondWith({ status: 400, contentType: 'text/plain', responseText: 'That email is already waiting for a reply' })

      expect(view.$('#message-box').text()).toBe('That email is already waiting for a reply')
      expect(view.$('.btn-submit').prop('disabled')).toBeFalse()
    })

    it('reports the status when the server gives no reason', () => {
      const request = submit()

      request.respondWith({ status: 502, statusText: 'Bad Gateway', contentType: 'text/plain', responseText: '' })

      expect(view.$('#message-box').text()).toBe('Status: 502 - Bad Gateway')
    })
  })
})
