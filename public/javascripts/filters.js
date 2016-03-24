/**
 * Created by Ott Konstantin on 17.03.2016.
 */

(function ($) {

    $(document).ready(function () {
        initializeFilters();
    });

    function initializeFilters(){
        //Initialize the show filters link
        $('a[href="display-filters"]').click(function(){
            var filtersForm = $('form#aspect_discovery_SimpleSearch_div_search-filters');
            filtersForm.show();
            filtersForm.css('visibility', 'visible');
            $(this).hide();
            return false;
        });

        $('.addfilter').click(function(){
            console.log('add ' + $(this));
            addFilterRow($(this));
            return false;
        });
        $('.removefilter').click(function(e){
            //console.log('remove ' + $(this));
            removeFilterRow($(this));
            return false;
        });
        //Disable the enter key !
        $('input[name^=filter_][type=text]').keypress(function(event){
            if(event.which == 13){
                //Entered pressed, do NOT submit the form, add a new filter instead !
                addFilterRow();
                event.preventDefault();
            }
        });

    }
    function removeFilterRow(button){
        //console.log('remove ' + button);
        if (button[0].id.indexOf('_0') >-1) {
            $('#filter_0').val("");
        } else {
            button.parent().parent().remove();
        }
    }

    function addFilterRow(button){
         var newFilterRow = $('#filterrow_0').clone();
        console.log("cloned" + newFilterRow);
        var rowid = button[0].id;
        var oldIndex = parseInt(rowid.substr(rowid.lastIndexOf('_')+1, rowid.length));
        var newIndex = oldIndex + 1;
        newFilterRow.attr('id','filterrow_' + newIndex);
        newFilterRow.find('#filtertype_0').attr('name', 'filtertype_' + newIndex);
        newFilterRow.find('#filtertype_0').attr('id', 'filtertype_' + newIndex);
        newFilterRow.find('#filter_relational_operator_0').attr('name', 'filter_relational_operator_' + newIndex);
        newFilterRow.find('#filter_relational_operator_0').attr('id', 'filter_relational_operator_' + newIndex);
        newFilterRow.find('#filter_0').attr('name', 'filter_' + newIndex);
        newFilterRow.find('#filter_0').attr('id', 'filter_' + newIndex);
        newFilterRow.find('#filter_' + newIndex).val('');
        newFilterRow.find('#addbutton_0').attr('id', 'addbutton_' + newIndex);
        newFilterRow.find('#removebutton_0').attr('id', 'removebutton_' + newIndex);
        console.log(oldIndex);
        newFilterRow.insertAfter($('#filterrow_'+oldIndex));
        // initialize new buttons
        var newAdd = newFilterRow.find('#addbutton_'+ newIndex);
        newAdd.click(function() {
            addFilterRow(newAdd);
            return false;
        });
        var newRem = newFilterRow.find('#removebutton_'+ newIndex);
        newRem.click(function() {
            removeFilterRow(newRem);
            return false;
        });
    }
    function addFilterRow1(){
        var previousFilterRow = $('tr#aspect_discovery_SimpleSearch_row_filter-controls').prev();
        //Duplicate our element & give it a new index !
        var newFilterRow = previousFilterRow.clone();
        //Alter the html to update the index
        var rowIdentifier = newFilterRow.attr('id');
        var oldIndex = parseInt(rowIdentifier.substr(rowIdentifier.lastIndexOf('-')+1, rowIdentifier.length));
        //Add one to the index !
        var newIndex = oldIndex + 1;
        //Update the index of all inputs & our list
        newFilterRow.attr('id', newFilterRow.attr('id').replace('-' + oldIndex, '-' + newIndex));
        newFilterRow.find('input, select').each(function(){
            var $this = $(this);
            //Update the index of the name (if present)
            $this.attr('name', $this.attr('name').replace('_' + oldIndex, '_' + newIndex));
            $this.attr('id', $this.attr('id').replace('_' + oldIndex, '_' + newIndex));
        });
        //Clear the values
        newFilterRow.find('input[type=text], select').val('');

        previousFilterRow = newFilterRow.insertAfter(previousFilterRow);
        //Initialize the add button
        previousFilterRow.find('input[name^="add-filter_"]').click(function(){
            addFilterRow();
            return false;
        });
        //Initialize the remove button
        previousFilterRow.find('input[name^=remove-filter_]').click(function(){
            removeFilterRow($(this));
            return false;
        });
    }
})(jQuery);