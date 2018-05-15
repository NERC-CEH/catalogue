<div class="row">
  <div class="col-sm-2">
    <label for="input-name<%= data.index %>">Fieldname</label>
  </div>
  <div class="col-sm-10">
    <input data-name="name" id="input-name<%= data.index %>" class="editor-input" value="<%= data.name %>" placeholder="name of field/column" />
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label for="input-title<%= data.index %>">Title</label>
  </div>
  <div class="col-sm-10">
    <input data-name="title" id="input-title<%= data.index %>" class="editor-input" value="<%= data.title %>" placeholder="A nicer human readable label for the field (optional)" />
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label for="input-description<%= data.index %>">Description</label>
  </div>
  <div class="col-sm-10">
    <textarea data-name="description" id="input-description<%= data.index %>" class="editor-textarea" rows="3"><%= data.description %></textarea>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-xs-12">
    <label for="input-type<%= data.index %>">Type</label>
  </div>
  <div class="col-sm-4 col-xs-12">
    <input list="typeList" data-name="type" id="input-type<%= data.index %>" class="editor-input" value="<%= data.type %>" placeholder="" />
  </div>
  <div class="col-sm-2 col-xs-12">
    <div class="hidden-xs text-right">
      <label for="input-format<%= data.index %>">Format</label>
    </div>
    <div class="visible-xs-inline">
      <label for="input-format<%= data.index %>">Format</label>
    </div>
  </div>
  <div class="col-sm-4 col-xs-12">
    <input list="formatList" data-name="format" id="input-format<%= data.index %>" class="editor-input" value="<%= data.format %>" placeholder="recommended for dates and times" />
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label for="input-units<%= data.index %>">Units</label>
  </div>
  <div class="col-sm-10">
    <input data-name="units" id="input-units<%= data.index %>" class="editor-input" value="<%= data.units %>"/>
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label for="input-maximum<%= data.index %>">Maximum value</label>
  </div>
  <div class="col-sm-4">
    <input data-name="maximum" id="input-maximum<%= data.index %>" class="editor-input" value="<%= data.maximum %>"/>
  </div>
  <div class="col-sm-2">
    <label for="input-minimum<%= data.index %>">Minimum value</label>
  </div>
  <div class="col-sm-4">
    <input data-name="minimum" id="input-minimum<%= data.index %>" class="editor-input" value="<%= data.minimum %>"/>
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label for="input-minLength<%= data.index %>">Minimum length</label>
  </div>
  <div class="col-sm-4">
    <input data-name="minLength" id="input-minLength<%= data.index %>" class="editor-input" value="<%= data.minLength %>"/>
  </div>
  <div class="col-sm-2">
    <label for="input-maxLength<%= data.index %>">Maximum length</label>
  </div>
  <div class="col-sm-4">
    <input data-name="maxLength" id="input-maxLength<%= data.index %>" class="editor-input" value="<%= data.maxLength %>"/>
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label>Unique</label>
  </div>
  <div class="col-sm-10">
      <label class="radio-inline">
        <input type="radio" data-name="unique" name="unique<%= data.index %>" value="true" <% if (data.unique == true) { %> checked="checked" <% } %> /> Yes 
      </label>
      <label class="radio-inline">
        <input type="radio" data-name="unique" name="unique<%= data.index %>" value="false" <% if (data.unique == false) { %> checked="checked" <% } %> />No
      </label>
  </div>
</div>

<datalist id="typeList">
  <option value="boolean">True or false</option>
  <option value="date">Date (without time)</option>
  <option value="datetime">Date AND time</option>
  <option value="number">Decimal number </option>
  <option value="email">Email address</option>
  <option value="geopoint">Geographic point (e.g. lon, lat)</option>
  <option value="integer">Integer</option>
  <option value="string">Text string</option>
  <option value="time">Time</option>
  <option value="uri">URI such as a web address or urn</option>
  <option value="uuid">UUID/GUID</option>
  <option value="year">Four digit year</option>
  <option value="yearmonth">Year and month (e.g. 2015-07)</option>
</datalist>
<datalist id="formatList">
  <option value="YYYY">Four digit year e.g. 2018</option>
  <option value="YYYY-MM">Year and month e.g. 2018-12</option>
  <option value="YYYY-MM-DD">ISO date e.g. 2018-12-25</option>
  <option value="HH:MM:SS">ISO time e.g. 13:30:25</option>
</datalist>