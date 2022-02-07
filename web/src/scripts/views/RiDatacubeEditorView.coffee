define [
	'cs!views/EditorView'
	'cs!views/editor/InputView'
	'cs!views/editor/TextareaView'
	'cs!views/editor/SingleObjectView'
	'cs!views/editor/SingleView'
	'cs!views/editor/SelectView'
	'cs!views/editor/ReadOnlyView'
	'cs!views/editor/ParentView'
	'cs!views/editor/ParentLargeView'
	'cs!views/editor/ParentStringView'
	'cs!views/editor/ParentStringTextboxView'
	'cs!views/editor/PredefinedParentView'
	'cs!views/editor/PredefinedParentLargeView'
  	'cs!models/editor/Contact'
  	'cs!views/editor/ContactView'
	'cs!views/editor/RelatedRecordView'

], (EditorView, InputView, TextareaView, SingleObjectView, SingleView, SelectView, ReadOnlyView, ParentView, ParentLargeView, ParentStringView, ParentStringTextboxView, PredefinedParentView, PredefinedParentLargeView, Contact, ContactView, RelatedRecordView) -> EditorView.extend

	initialize: ->
		@model.set('type', 'riDatacube') unless @model.has('type')

		@sections = [
			label: ' General '
			title: 'General'
			views: [
				new InputView
					model: @model
					modelAttribute: 'title'
					label: 'Name'
					helpText: """
                    <p>Should reflect purpose (succinctly)</p><p>Should be consistent (within and across assets)</p>
                    """

				new TextareaView
				model: @model
				modelAttribute: 'description'
				label: 'Description'
				rows: 8

				new SelectView
					model: @model
					modelAttribute: 'infrastructureClass'
					label: 'Infrastructure class'			
					options: [
						{value: '', label: ''},
						{value: 'Environmental observatories', label: 'Class 1: Environmental observatories'},
						{value: 'Environmental experiment platforms', label: 'Class 2: Environmental experiment platforms'},
						{value: 'Analytical facilitie', label: 'Class 3: Analytical facilities'},
						{value: 'Digital infrastructure', label: 'Class 4: Digital infrastructure'}
					]

				new InputView
					model: @model
					modelAttribute: 'infrastructureCategory'
					label: 'Infrastructure category'				

				new TextareaView
					model: @model
					modelAttribute: 'purpose'
					label: 'Purpose'				

				new TextareaView
					model: @model
					modelAttribute: 'capabilities'
					label: 'Capabilities'				

				new TextareaView
					model: @model
					modelAttribute: 'lifecycle'
					label: 'Lifecycle'				

				new TextareaView
					model: @model
					modelAttribute: 'uniqueness'
					label: 'Uniqueness'				

				new TextareaView
					model: @model
					modelAttribute: 'partners'
					label: 'Partners'				
									
				new ParentStringView
					model: @model
					modelAttribute: 'users'
					label: 'Users'		

				new TextareaView
					model: @model
					modelAttribute: 'access'
					label: 'Access'				

				new TextareaView
					model: @model
					modelAttribute: 'userCosts'
					label: 'User costs'	

				new TextareaView
					model: @model
					modelAttribute: 'fundingSources'
					label: 'Funding sources'

				new PredefinedParentView
					model: @model
					ModelType: Contact
					modelAttribute: 'owners'
					label: 'Owner'
					ObjectInputView: ContactView
					multiline: true
					predefined:
						'UKCEH Bangor':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							email: 'enquiries@ceh.ac.uk'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Bangor'
						'UKCEH Edinburgh':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							email: 'enquiries@ceh.ac.uk'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Edinburgh'
						'UKCEH Lancaster':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							email: 'enquiries@ceh.ac.uk'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Lancaster'
						'UKCEH Wallingford':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							email: 'enquiries@ceh.ac.uk'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Wallingford'

				new TextareaView
					model: @model
					modelAttribute: 'locationText'
					label: 'Location'				

				new TextareaView
					model: @model
					modelAttribute: 'scienceArea'
					label: 'Science area'

				new ParentView
					model: @model
					modelAttribute: 'relatedRecords'
					label: 'Related records'
					ObjectInputView: RelatedRecordView
					multiline: true				
			]
		]

		EditorView.prototype.initialize.apply @
