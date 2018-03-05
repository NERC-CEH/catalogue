# New Form

this is to document how to create a new form

some examples to follow are Sensor and Manufacturer

## Approach

instead of using backbone to create forms use freemarker to create forms. This allows a more secure way to access data which the clinet shouldn't otherwise know, e.g. Jira issues. Thus removing all business logic from the client.

NOTE: Trying to make form "borderless" and create borders by aligning everything

## Setup

create a standard document following <./new-catalogue.md>

in `WebConfig.json` your `DOCUMENT_${DOCUMENT_NAME}_JSON_VALUE` needs to be of the format `application/vnd.${document-name}-document+json` as the coffeescript works off this convention

### Coffeescript

in `main.coffee` you need to update `initEditor` with

```coffeescript
'${DOCUMENT_${DOCUMENT_NAME}_SHORT}':
  usesNewForm: true
  View: NewEditorView
  Model: EditorMetadata
  mediaType: 'application/vnd.${document-name}-document+json'
```

that is all you need to do

## Freemarker

The rest of the code goes into your freemarker code, it uses the `new-form.html.tpl` template

### Master

```ftl
<#import "${relative-path-to-root}/skeleton.html.tpl" as skeleton>
<#import "${relative-path-to-root}/new-form.html.tpl" as form>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='${document-name}'>
    </@form.master>
</@skeleton.master>
```

the `document-name` comes from `DOCUMENT_${DOCUMENT_NAME}_JSON_VALUE` and is the same as `application/vnd.${document-name}-document+json` which is why the coffeescript needs no configuration

the rest of the code goes inside `<@form.master document='${document-name}'>${HERE}</@form.master>`

there are 3 parts to a new form `head`, `body`, and `admin`

### Head

This sits ontop of all the "tabulated" data

```ftl
<@form.head>
    <@form.title title=title></@form.title>
    <@form.description description=description></@form.description>
</@form.head>
```

Both title and description are optional. If the user has `view` permissions and no other permissions, then the title and description are static

### Body

This is the "tabulated" section and you can place anything in here

```ftl
<@form.body>
    ${your content}
</@form.body>
```

### Admin

This is a placeholder because admin experience is a little disjoined with this form look at `sensor.html.tpl` and `elter/_admin.html.tpl` for an example admin block. All you need is the wrapper the `new-admin` class will add some padding to the admin section

```ftl
<div class="container new-admin">
</div>
```

## Form Components

A list of components you can how, with example usage

### Value

this is a block which creates a "tabulated" experience with a `label` on the left and the `contents` on the right

```ftl
<@form.value name="name" label="A Label" href="optional-url" class="optional class" hidden=false|true errorMessage="optional error message">
    ${contents}
</@form.value>
```

* `name` used in the coffeescript for error validation and updating the `href`. This value should match the `name` of the `input`, `select` or `textarea` that is inside the value block e.g. `<input name="title">` then `name="title"`
* `label` the value which users can see on the left
* `href` to turn label into a hyperlink. Useful for linking documents or external documentation 
* `class` added to default class
* `hidden` to hide the value block. Example usage, in a `select` when selecting `Other` then change this to visible or selecting `no` in a radio button so to answer `Why no?`
* `errorMessage` shown when input fails validation. Your input must have the same name as the value block for this to work. Validates on `pattern` and `required` no validation for `disabled` inputs.

### Input

```ftl
<@form.input name="input-name" type="text" class="optional class" value="optional value" placeholder="optional placeholder" id="optional id" pattern="optional pattern" readonlyValue="optional value">
</@form.input
```

* `name` standard input `name`
* `type` standard input `type`
* `class` added to default class
* `value` standard input `value`
* `placeholder` standard input `value`
* `id` standard input `id`
* `pattern` standard input `pattern` for validation
* `readonlyValue` if user has `view` permission and there is no `value` then this is the displayed value e.g. you might have `No Title`

### Select

```ftl
<@form.select name="name" id="optional id" class="optional class" value="optional value" readonlyValue="optional value">
    <option value="1">One</option>
    <option value="2">Two</option>
</@form.select>
```

* `name` standard select `name`
* `class` added to default class
* `value` if this value is blank will change the color of the readonly value
* `id` standard input `id`
* `readonlyValue` if user has `view` permission and there is no `value` then this is the displayed value e.g. you might have `No Title`

### Static

If you want to put static data in the document

```ftl
<@form.static class="optional class">
    <span>Your content</span>
</@form.static>
```

### Delete

a red times button

```ftl
<@form.delete></@form.delete>
```

you can then use `$('.delete-${delete-name})` in order add events to the delete button, you are likly to have more than one of these

### Read Only and Disabled

Is `readonly` and `disabled` if the user has `view` permissions but no `edit` permissions

```ftl
<@form.ifReadOnly>Your Content</@form.ifReadOnly>
<@form.ifNotReadOnly>Your Content</@form.ifNotReadOnly>
<@form.ifDisabled>Your Content</@form.ifDisabled>
<@form.ifNotDisabled>Your Content</@form.ifNotDisabled>
```

### Value Block and Value Block Value

this part is still under construction but in your value you may want to do some flex styling, this should help keep elements in-line

here is an example which has a delete button next to a select with a hyperlink underneath

```ftl
<div class='value-block'>
<@form.delete></@form.delete>
    <div class='value-block-value'>
        <@form.select name="name[0]">
            <#list linkedDocuments as doc>
                    <option <#if doc.id == myLinkedDoc>selected</#if> value="${doc.id}">${doc.title}</option>
                </#if>
            </#list>
            <option value="other">Other</option>
        </@form.select>
        <div>
            <@form.static>
                <a href="/documents/${myLinkedDoc.id}">${myService.getLinkedDocs(myLinkedDoc.id).title}</a>
            </@form.static>
        </div>
    </div>
</div>
```

this part is likly to change

## Bespoke Elements

Add your own bespoke form elements which requires bespoke coffeescript. Update `NewEditorView.coffee` by requiring `'cs!views/MyCatalogueViewFunctions'` which is a file containing

```coffeescript
define [], () -> (view, model) ->
  myFunc ->
    # some logic
  myOtherFunc ->
    # some other logic
  -> # NOTE
    do myFunc
    do myOtherFunc
```

**NOTE** A plain function returned and not an object, it is plain coffeescript, this means you don't need to reference `@`

Tthe `view` is the `NewEditorView` and the `model` is `EditorMetadata` i.e. your document

Then update `NewEditorView.coffee` by updating `addedFns` with `@fns.myCatalogue = MyCatalogueViewFunctions(@, @model)`

Called everytime model updates. Good practice is to run `unbind` before you bindnig events such as, `click` e.g.

```coffeescript
$('selector').unbnid 'click'
$('selector').click ->
    # logic
```

### updateOtherable

still under construction but the idea that you want to add a new document or value as an alternative to a list of known documents or values i.e. the `Other` in a select

## Disable Auto Save

under construction but you can disable the autosave feature by adding an element with `.save` in your form then the user will have to click this save button in order to save. Should be avoided as autosave is just the best

# Missing and Thoughts

* multi pages, some forms have multiple pages. This is an accessibility nightmare because you are hiding values in the form with no prompt how to resolve this. Prefered solution is to break document down into sub documents. Another solution is to let the form get long and break up the sections with `<hr />` and `<h4>Section Title<h4>`
