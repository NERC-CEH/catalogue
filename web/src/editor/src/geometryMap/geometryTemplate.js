import _ from 'underscore'

export default _.template(`
<div class="geometryEditor">
    <div class="map" style="width: 500px; height: 500px;"></div>
    <div>
        <label>Advanced: Edit JSON geometry
            <!--<button class="editor-button-xs showhide" title="show/hide details"><span class="fa-solid fa-chevron-down" aria-hidden="true"></span></button>-->
            <textarea class="box editor-textarea" data-name="geometryString"><%= data.geometryString %></textarea>
        </label>
    </div>

    <% if (data.showConfidentialCheckbox === true) { %>
        <div class="form-check form-switch">
            <label>
                <input type="checkbox" class="form-check-input locationConfidential" <% if(data.locationConfidential === true) { %> checked <% } %> role="switch" >
                Location is confidential
            </label>
            <p class="text-body-secondary">
                The location will be obfuscated to an area 0.5&deg; &times; 0.5&deg; (approximately 11 km &times; 7 km).
                The precise location is not saved, and unticking this box will not restore it.
            </p>
        </div> 
       
    <% } %>
 </div>
`)
