define [
	'cs!views/EditorView'
	'cs!views/editor/InputView'
	'cs!views/editor/TextareaView'
	'cs!views/editor/SingleObjectView'
	'cs!views/editor/SingleView'
	'cs!views/editor/SelectView'
	'cs!views/editor/ReadOnlyView'
	'cs!views/service-agreement/TextOnlyView'
	'cs!views/editor/ParentView'
	'cs!views/editor/ParentLargeView'
	'cs!views/editor/ParentStringView'
	'cs!views/editor/ParentStringTextboxView'
	'cs!views/editor/PredefinedParentView'
	'cs!views/editor/PredefinedParentLargeView'
	'cs!models/editor/Contact'
	'cs!views/editor/ContactView'
	'cs!views/editor/RelatedRecordView'
], (EditorView, InputView, TextareaView, SingleObjectView, SingleView, SelectView, ReadOnlyView, TextOnlyView, ParentView, ParentLargeView, ParentStringView, ParentStringTextboxView, PredefinedParentView, PredefinedParentLargeView, Contact, ContactView, RelatedRecordView) -> EditorView.extend

	initialize: ->
		@model.set('type', 'riDatacube') unless @model.has('type')

		@sections = [
			label: 'General'
			title: 'General'
			views: [
				new InputView
					model: @model
					modelAttribute: 'title'
					label: 'Name'
					helpText: """
                    	<p>Should reflect purpose (succinctly)</p><p>Should be consistent (within and across assets)</p>
                    """

				new TextOnlyView
					model: @model
					label: 'Purpose'
					text: """<p>Explain strategic relevance: What does it do? Why? Who cares?</p><p>Write in plain English and avoid (or define) acronyms.</p><p>Explain relevance to government policy agenda</p>
							"""

				new TextareaView
					model: @model
					modelAttribute: 'description'
					rows: 3

				new SelectView
					model: @model
					modelAttribute: 'infrastructureCategory'
					label: 'Infrastructure category'
					options: [
						{value: 'Instrumented sites', label: 'Instrumented sites'},
						{value: 'Periodic surveys', label: 'Periodic surveys'},
						{value: 'Wildlife monitoring schemes', label: 'Wildlife monitoring schemes'},
						{value: 'Discovery collections and archives', label: 'Discovery collections and archives'},
						{value: 'Mobile observing platforms', label: 'Mobile observing platforms'},
						{value: 'Controlled environment facilities (micro- or mesocosms)', label: 'Controlled environment facilities (micro- or mesocosms)'},
						{value: 'Field research facilities (macrocosms)', label: 'Field research facilities (macrocosms)'},
						{value: 'Legacy Experimental Platforms or sites', label: 'Legacy Experimental Platforms or sites'},
						{value: 'Analysis facilities', label: 'Analysis facilities'},
						{value: 'Test facilities', label: 'Test facilities'},
						{value: 'Digital computing platforms ', label: 'Digital computing platforms'},
						{value: 'Data and information', label: 'Data and information'},
						{value: 'Digital labs for data analytics', label: 'Digital labs for data analytics'},
						{value: 'UKCEH models', label: 'UKCEH models'},
						{value: 'Community models', label: 'Community models'},
					]

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
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Bangor'
						'UKCEH Edinburgh':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Edinburgh'
						'UKCEH Lancaster':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Lancaster'
						'UKCEH Wallingford':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Wallingford'

				new TextareaView
					model: @model
					modelAttribute: 'locationText'
					label: 'Location'				

				new TextareaView
					model: @model
					modelAttribute: 'capabilities'
					label: 'Capabilities'				
					helpText: """
                    	<p>Describe the facility, experimental design.  What is it equipped to do or measure?</p><p>Be informative for external partners and users</p>
                    """

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
					helpText: """
                    	<p>Include all funding sources</p><p>Be specific about NC awards/programmes</p>
                    """

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
