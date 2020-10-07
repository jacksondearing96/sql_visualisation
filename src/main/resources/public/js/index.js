function activateToggleColumnsButton() {
    $('.slider').css('opacity', 1);
    $('#show-columns-toggle-switch').attr('disabled', false);
}

function generateVisualisationButtonClicked() {
    $.post("/lineage_extractor", sql, lineageNodes => {
        let graph = backendToFrontendDataStructureConversion(JSON.parse(lineageNodes));
        generateVisualisation(graph);
    });
    activateToggleColumnsButton();
}

function toggleColumns() {
    showColumns = showColumns ? false : true;
    $('.column').css('display', showColumns ? 'block' : 'none');
    showColumnsChanged = true;
    ticked();
}

function initialiseEventListeners() {
    uploadFilesInput.change(uploadFiles);
    chooseFilesButton.click(() => uploadFilesInput.trigger('click'));
    generateVisualisationButton.click(generateVisualisationButtonClicked);
    demoButton.click(() => generateVisualisation(demoGraph));
    searchInput.keydown(() => setTimeout(searchInputChanged, 50));
    showColumnsToggleSwitch.click(toggleColumns);
}

$(document).ready(initialiseEventListeners);