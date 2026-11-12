import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
import Backbone from 'backbone'
import { AuthorView, ContactView } from '../src/views'
import { Contact } from '../src/models'
import { fakeServer, settle } from '../../test/support'
import { autocompleteOf } from '../src/utils/autocomplete'
import { orcidVerdict, personDetails, personOption } from '../src/utils/peopleUtils'

// Helping editors give people their ORCID iD, within what ORCID allows (dri-one #534): only iDs the
// catalogue already holds are offered, and every iD given is checked with ORCID's registry
describe('People and their ORCID iDs', () => {
  const clare = {
    orcid: 'https://orcid.org/0000-0002-0459-506X',
    givenName: 'Clare',
    familyName: 'Rowland',
    otherNames: ['C. Rowland'],
    affiliations: [{ name: 'UK Centre for Ecology & Hydrology', identifier: 'https://ror.org/00pggkr55' }],
    records: 3
  }
  const namesake = { orcid: 'https://orcid.org/0000-0002-5474-5257', givenName: 'Clare', familyName: 'Rowland', affiliations: [], records: 1 }

  describe('describing a person', () => {
    it('says their iD, where they work and how many records name them', () => {
      expect(personDetails(clare)).toBe('ORCID 0000-0002-0459-506X · UK Centre for Ecology & Hydrology · in 3 records')
      expect(personDetails(namesake)).toBe('ORCID 0000-0002-5474-5257 · in 1 record')
    })

    it('shows their name and how else records name them, as text', () => {
      const option = personOption({ ...clare, givenName: '<b>Clare</b>' })
      expect(option.querySelector('.person-name').textContent).toBe('<b>Clare</b> Rowland')
      expect(option.querySelector('b')).toBeNull()
      expect(option.querySelector('.person-alias').textContent).toBe('also named C. Rowland')
    })
  })

  describe('what ORCID says of an iD', () => {
    const record = { givenNames: 'Clare', familyName: 'Rowland', creditName: '', otherNames: ['Clare S. Rowland'] }

    it('confirms the name typed', () => {
      expect(orcidVerdict(record, 'Clare', 'Rowland')).toEqual({ level: 'success', text: '✓ ORCID\'s record for this iD: Clare Rowland' })
      expect(orcidVerdict(record, 'C.', 'rowland')).toEqual({ level: 'success', text: '✓ ORCID\'s record for this iD: Clare Rowland' })
    })

    it('warns when ORCID names someone else', () => {
      expect(orcidVerdict(record, 'Claire', 'Rowlands')).toEqual({
        level: 'warning',
        text: 'ORCID\'s record for this iD is Clare Rowland, not Claire Rowlands. Check it is the same person.'
      })
    })

    it('matches a name the person also goes by', () => {
      expect(orcidVerdict({ givenNames: 'Josiah', familyName: 'Carberry', otherNames: ['J. S. Carberry-Smith'] }, 'J.', 'Carberry-Smith').level).toBe('success')
    })

    it('says an iD ORCID never issued', () => {
      expect(orcidVerdict(null, 'Clare', 'Rowland').level).toBe('danger')
    })

    it('says when the holder keeps their name private', () => {
      expect(orcidVerdict({ givenNames: '', familyName: '' }, 'Clare', 'Rowland').level).toBe('warning')
    })

    it('just says whose it is when no name has been typed', () => {
      expect(orcidVerdict(record, '', '')).toEqual({ level: 'body-secondary', text: 'ORCID\'s record for this iD: Clare Rowland' })
    })
  })

  describe('in a contact', () => {
    const json = body => ({ status: 200, contentType: 'application/json', responseText: JSON.stringify(body) })
    const requests = path => fakeServer.requests().filter(request => decodeURIComponent(request.url).startsWith(path))
    const place = view => { $(document.body).append(view.el); return view }
    let searching = null

    beforeEach(() => fakeServer.install())

    afterEach(() => {
      fakeServer.uninstall()
      $('.autocomplete-menu').remove()
      $(document.body).empty()
    })

    describe('finding a person', () => {
      const views = [
        ['a contact', ContactView, Contact],
        ['a service agreement\'s author', AuthorView, Backbone.Model]
      ]

      // Distinct iDs for each view, as the editor remembers what ORCID said of an iD for the page
      const iDs = [
        { found: '0000-0004-9876-5414', never: '0000-0000-0000-0028', down: '0000-0004-9876-5430' },
        { found: '0000-0004-9876-5422', never: '0000-0000-0000-0036', down: '0000-0004-9876-5449' }
      ]

      views.forEach(([who, View, Model], index) => {
        describe(`for ${who}`, () => {
          let model, view, $find

          beforeEach(() => {
            model = new Model()
            view = place(new View({ model, index: 'p1' }))
            $find = view.$('.personAutocomplete')
          })

          const search = term => { $find.val(term); searching = autocompleteOf($find).search() }
          const answer = async response => { fakeServer.lastRequest().respondWith(response); await searching }
          const options = () => [...autocompleteOf($find).menu.querySelectorAll('[role=option]')]

          it('looks up only the people the catalogue already names', async () => {
            search(' clare row ')

            expect(decodeURIComponent(fakeServer.lastRequest().url)).toBe('/people/names?query=clare row')
            await answer(json([clare, namesake]))
            expect(options().map(option => option.textContent)).toEqual([
              'Clare RowlandORCID 0000-0002-0459-506X · UK Centre for Ecology & Hydrology · in 3 recordsalso named C. Rowland',
              'Clare RowlandORCID 0000-0002-5474-5257 · in 1 record'
            ])
          })

          it('fills in the name, iD and organisation of the person chosen, and empties the look-up', async () => {
            search('clare')
            await answer(json([clare]))

            options()[0].click()

            expect(model.get('givenName')).toBe('Clare')
            expect(model.get('familyName')).toBe('Rowland')
            expect(model.get('nameIdentifier')).toBe('https://orcid.org/0000-0002-0459-506X')
            expect(model.get('organisationName')).toBe('UK Centre for Ecology & Hydrology')
            expect(model.get('organisationIdentifier')).toBe('https://ror.org/00pggkr55')
            expect(view.$('[data-name=familyName]').val()).toBe('Rowland')
            expect(view.$('[data-name=nameIdentifier]').val()).toBe('https://orcid.org/0000-0002-0459-506X')
            expect($find.val()).toBe('')
          })

          it('keeps an organisation already given', async () => {
            model.set('organisationName', 'Lancaster University')
            search('clare')
            await answer(json([clare]))

            options()[0].click()

            expect(model.get('organisationName')).toBe('Lancaster University')
          })

          it('says when nobody by that name has an iD in the catalogue', async () => {
            search('nobody')
            await answer(json([]))

            expect(options().map(option => option.textContent)).toEqual(['Nobody by that name has an ORCID in the catalogue yet. Paste their ORCID iD here, or enter their details below.'])
          })

          // An iD the editor already has, for someone the catalogue doesn't hold, is checked with ORCID's
          // registry and the person it names offered: reading one record by iD, not searching by name
          describe('given an iD the catalogue doesn\'t hold', () => {
            const searchFor = async (iD, orcidAnswer) => {
              search(iD)
              fakeServer.lastRequest().respondWith(json([]))
              await settle()
              if (orcidAnswer) {
                expect(decodeURIComponent(fakeServer.lastRequest().url)).toMatch(/^\/people\/orcid\?id=/)
                fakeServer.lastRequest().respondWith(orcidAnswer)
              }
              await searching
            }

            it('offers the person ORCID names, and fills in their name and iD', async () => {
              const { found } = iDs[index]
              await searchFor(`orcid.org/${found}`, json({ orcid: `https://orcid.org/${found}`, givenNames: 'Jane', familyName: 'Doe' }))

              expect(options().map(option => option.textContent)).toEqual([`Jane DoeORCID ${found} · from ORCID, not yet in the catalogue`])
              options()[0].click()

              expect(model.get('givenName')).toBe('Jane')
              expect(model.get('familyName')).toBe('Doe')
              expect(model.get('nameIdentifier')).toBe(`https://orcid.org/${found}`)
              expect(model.get('organisationName')).toBeUndefined()
              expect(view.$('[data-name=givenName]').val()).toBe('Jane')
            })

            it('says ORCID has no record of an iD it never issued', async () => {
              await searchFor(iDs[index].never, { status: 404, responseText: '' })

              expect(options().map(option => option.textContent)).toEqual(['ORCID has no record of this iD. Check it was copied correctly.'])
            })

            it('says when ORCID can\'t be reached', async () => {
              await searchFor(iDs[index].down, { status: 502, responseText: '' })

              expect(options().map(option => option.textContent)).toEqual(['ORCID couldn\'t be reached to check this iD. Paste it into the ORCID iD field below.'])
            })
          })

          it('says an iD with a wrong check character isn\'t real, without asking anyone', async () => {
            const before = fakeServer.requestCount()
            search('0000-0002-1825-0098')
            await searching

            expect(fakeServer.requestCount()).toBe(before)
            expect(options().map(option => option.textContent)).toEqual(['That isn\'t a real ORCID iD: its last character doesn\'t match the rest. Check it was copied correctly.'])
          })

          it('keeps the look-up out of the record', () => {
            $find.val('clare').trigger('change')

            expect(model.has('undefined')).toBe(false)
            expect(Object.keys(model.attributes).filter(key => !['role', 'address'].includes(key))).toEqual([])
          })
        })
      })

      it('isn\'t offered for an organisation\'s contact', () => {
        const view = place(new ContactView({ model: new Contact(), index: 'p2', roleDefault: 'custodian', personLookup: false }))

        expect(view.$('.personFind').hasClass('d-none')).toBe(true)
        expect(autocompleteOf(view.$('.personAutocomplete'))).toBeUndefined()
      })
    })

    // The organisation and the person there are two things, collected in two labelled groups
    it('groups the organisation\'s fields apart from the person\'s', () => {
      const view = place(new ContactView({ model: new Contact(), index: 'g1' }))
      const fieldsIn = group => view.$(`fieldset.contact-group-${group} [data-name]`).map((i, el) => $(el).data('name')).get()

      expect(view.$('fieldset.contact-group-organisation > legend').text().trim()).toBe('Organisation')
      expect(fieldsIn('organisation')).toEqual(['organisationName', 'organisationIdentifier'])
      expect(view.$('fieldset.contact-group-person > legend').text()).toMatch(/^\s*Person/)
      expect(fieldsIn('person')).toEqual(['honorificPrefix', 'givenName', 'familyName', 'nameIdentifier', 'displayName'])
      expect(view.$('fieldset.contact-group-person .personAutocomplete')).toHaveLength(1)
    })

    describe('giving an iD', () => {
      it('keeps one pasted bare as https://orcid.org/…', () => {
        const model = new Contact()
        const view = place(new ContactView({ model, index: 'p3' }))

        view.$('[data-name=nameIdentifier]').val('0000-0003-1234-572x').trigger('change')

        expect(model.get('nameIdentifier')).toBe('https://orcid.org/0000-0003-1234-572X')
        expect(view.$('[data-name=nameIdentifier]').val()).toBe('https://orcid.org/0000-0003-1234-572X')
      })

      it('keeps one pasted bare as https://orcid.org/… for a service agreement\'s author', () => {
        const model = new Backbone.Model()
        const view = place(new AuthorView({ model, index: 'p4' }))

        view.$('[data-name=nameIdentifier]').val('orcid.org/0000-0003-1234-5711').trigger('change')

        expect(model.get('nameIdentifier')).toBe('https://orcid.org/0000-0003-1234-5711')
      })

      it('checks it with ORCID and says whose it is', async () => {
        const model = new Contact({ givenName: 'Clare', familyName: 'Rowland', nameIdentifier: 'https://orcid.org/0000-0003-1234-5674' })
        const view = place(new ContactView({ model, index: 'p5', personLookup: false }))
        const $status = view.$('.orcid-status')

        expect(decodeURIComponent(fakeServer.lastRequest().url)).toBe('/people/orcid?id=0000-0003-1234-5674')
        expect($status.text()).toBe('Checking this iD with ORCID…')
        fakeServer.lastRequest().respondWith(json({ givenNames: 'Clare', familyName: 'Rowland' }))
        await settle()

        expect($status.text()).toBe('✓ ORCID\'s record for this iD: Clare Rowland')
        expect($status.hasClass('text-success')).toBe(true)
        expect(view.$('[data-name=nameIdentifier]').attr('aria-describedby')).toContain($status.attr('id'))
      })

      it('says ORCID has no record of an iD it never issued', async () => {
        const model = new Contact({ familyName: 'Rowland', nameIdentifier: 'https://orcid.org/0000-0003-1234-5682' })
        const view = place(new ContactView({ model, index: 'p6', personLookup: false }))

        fakeServer.lastRequest().respondWith({ status: 404, responseText: '' })
        await settle()

        expect(view.$('.orcid-status').text()).toBe('ORCID has no record of this iD. Check it was copied correctly.')
      })

      it('says ORCID couldn\'t be reached, and asks again next time', async () => {
        const model = new Contact({ familyName: 'Rowland', nameIdentifier: 'https://orcid.org/0000-0003-1234-5690' })
        const view = place(new ContactView({ model, index: 'p7', personLookup: false }))

        fakeServer.lastRequest().respondWith({ status: 502, responseText: '' })
        await settle()
        expect(view.$('.orcid-status').text()).toMatch(/^ORCID couldn't be reached/)

        place(new ContactView({ model: new Contact({ nameIdentifier: 'https://orcid.org/0000-0003-1234-5690' }), index: 'p8', personLookup: false }))
        expect(requests('/people/orcid?id=0000-0003-1234-5690')).toHaveLength(2)
      })

      it('asks ORCID once about an iD, however many contacts give it', async () => {
        const iD = 'https://orcid.org/0000-0003-1234-5703'
        place(new ContactView({ model: new Contact({ nameIdentifier: iD }), index: 'p9', personLookup: false }))
        fakeServer.lastRequest().respondWith(json({ givenNames: 'Josiah', familyName: 'Carberry' }))
        await settle()

        const view = place(new ContactView({ model: new Contact({ nameIdentifier: iD }), index: 'p10', personLookup: false }))
        await settle()

        expect(requests('/people/orcid')).toHaveLength(1)
        expect(view.$('.orcid-status').text()).toBe('ORCID\'s record for this iD: Josiah Carberry')
      })

      it('doesn\'t ask about an iD whose check character is wrong', () => {
        place(new ContactView({ model: new Contact({ nameIdentifier: 'https://orcid.org/0000-0002-1825-0098' }), index: 'p11', personLookup: false }))

        expect(requests('/people/orcid')).toHaveLength(0)
      })
    })

    describe('a person with no iD', () => {
      it('is shown anyone of that name the catalogue has an iD for, to use only if it is them', async () => {
        const model = new Contact({ givenName: 'C.', familyName: 'Rowland' })
        const view = place(new ContactView({ model, index: 'p12' }))

        expect(decodeURIComponent(fakeServer.lastRequest().url)).toBe('/people/names?query=C. Rowland')
        fakeServer.lastRequest().respondWith(json([clare, { ...namesake, givenName: 'Bob' }]))
        await settle()

        const $suggestions = view.$('.orcid-suggestions')
        expect($suggestions.prop('hidden')).toBe(false)
        expect($suggestions.find('p').text()).toBe('The catalogue already has an ORCID for someone of this name. Use it only if it is the same person:')
        expect($suggestions.find('.orcid-suggestion')).toHaveLength(1)

        $suggestions.find('button').trigger('click')

        expect(model.get('nameIdentifier')).toBe('https://orcid.org/0000-0002-0459-506X')
        expect(model.get('givenName')).toBe('C.')
        expect(view.$('[data-name=nameIdentifier]').val()).toBe('https://orcid.org/0000-0002-0459-506X')
        expect($suggestions.prop('hidden')).toBe(true)
      })

      it('isn\'t looked up for an organisation\'s contact', () => {
        place(new ContactView({ model: new Contact({ givenName: 'Clare', familyName: 'Rowland' }), index: 'p13', personLookup: false }))

        expect(requests('/people/names')).toHaveLength(0)
      })
    })
  })
})
