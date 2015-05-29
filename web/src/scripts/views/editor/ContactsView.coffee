define [
  'cs!views/editor/ParentView'
  'tpl!templates/editor/Contacts.tpl'
  'cs!views/editor/ContactsItemView'
  'cs!models/editor/Contact'
], (ParentView, template, ContactsItemView, Contact) -> ParentView.extend
  template:       template
  ModelType:      Contact
  modelAttribute: 'responsibleParties'
  ChildView:      ContactsItemView