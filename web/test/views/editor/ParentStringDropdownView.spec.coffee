define [
  'cs!views/editor/ParentStringDropdownView'
  'cs!models/Catalogue'
], (ParentStringDropdownView, Catalogue) ->
  describe 'ParentStringDropdownView', ->
    view = model = null

    beforeEach ->
      model = new Catalogue()
      view = new ParentStringDropdownView
        model: model
        modelAttribute: 'catalogues'
        label: 'Catalogues'
        options: [
              value: ''
              label: '- Select Catalogue -'
            ,
              value: 'Environmental Information Data Centre'
              label: 'Environmental Information Data Centre'
          ]

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is created with populated model', ->

      beforeEach ->
        model = new Catalogue
          catalogues: [ 'Environmental Information Data Centre' ]

        view = new ParentStringDropdownView
          model: model
          modelAttribute: 'catalogues'
          label: 'Catalogues'
          options: [
              value: ''
              label: '- Select Catalogue -'
            ,
              value: 'Environmental Information Data Centre'
              label: 'Environmental Information Data Centre'
          ]
        do view.render

      xit 'should be a array', ->
        expect(view.array).toBeDefined()

      xit 'should be populated from Catalogue model', ->
        expect(view.array[0]).toEqual 'Environmental Information Data Centre'
