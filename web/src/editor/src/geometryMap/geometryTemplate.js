import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-6 col-lg-6">
        <div class="row">
            <div class="map" style="width: 500px; height: 500px;"></div>
        </div>
    </div>
    <div class="col-sm-2 col-lg-2">
        <label>Advanced: Edit Geometry Json</label>
        <!--<button class="editor-button-xs showhide" title="show/hide details"><span class="fa-solid fa-chevron-down" aria-hidden="true"></span></button>-->
        <textarea rows="20" data-name="geometry" id="box" class="editor-input" value="<%= data.geometry %>" style="width: 500px; height: 250px;"><%= data.geometry %></textarea>
        <br>
    </div>
</div>
<br>
`)
