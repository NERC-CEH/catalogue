import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label for="schema-name<%= data.index %>">Field</label>
    </div>
    <div class="col-sm-5">
        <input data-name="name" id="schema-name<%= data.index %>" class="editor-input" value="<%= data.name %>" placeholder="name of field/column" />
    </div>
    <div class="col-sm-1">
        <label for="schema-type<%= data.index %>">Type</label>
    </div>
    <div class="col-sm-4">
        <input list="dataTypeList" data-name="type" id="schema-type<%= data.index %>" class="editor-input" value="<%= data.type %>" autocomplete="off" aria-autocomplete="none" />
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="schema-title<%= data.index %>">Title</label>
    </div>
    <div class="col-sm-10">
        <input data-name="title" id="schema-title<%= data.index %>" class="editor-input" value="<%= data.title %>" placeholder="A nicer human readable label for the field (optional)" />
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="schema-description<%= data.index %>">Description</label>
    </div>
    <div class="col-sm-10">
        <textarea data-name="description" id="schema-description<%= data.index %>" class="editor-textarea" rows="2"><%= data.description %></textarea>
    </div>
</div>

<datalist id="dataTypeList">
    <option value="float">Decimal number/float</option>
    <option value="integer">Integer</option>
    <option value="text">Text string</option>
    <option value="boolean">True or false</option>
    <option value="date">Date (without time)</option>
    <option value="time">Time</option>
    <option value="dateTime">Date AND time</option>
</datalist>
`)
