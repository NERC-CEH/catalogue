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

```ftl
```

### Input

### Select

### Static

### Delete