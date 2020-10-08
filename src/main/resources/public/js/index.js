function activateToggleButtons() {
    $('.slider').css('opacity', 1);
    showColumnsToggleSwitch.attr('disabled', false);
    staticModeToggleSwitch.attr('disabled', false);
}

function generateVisualisationButtonClicked() {
    $.post("/lineage_extractor", sql, lineageNodes => {
        let graph = backendToFrontendDataStructureConversion(JSON.parse(lineageNodes));
        generateVisualisation(graph);
    });
}

function toggleColumns() {
    showColumns = showColumns ? false : true;
    $('.column').css('display', showColumns ? 'block' : 'none');
    setGridStartingHeights();
    optimiseVerticalPadding();
    showColumnsChanged = true;
    ticked();
}

function toggleStaticMode() {
    staticMode = !staticMode;
    ticked();
}

function initialiseEventListeners() {
    uploadFilesInput.change(uploadFiles);
    chooseFilesButton.click(() => uploadFilesInput.trigger('click'));
    generateVisualisationButton.click(generateVisualisationButtonClicked);
    demoButton.click(() => generateVisualisation(demoGraph));
    searchInput.keydown(() => setTimeout(searchInputChanged, 50));
    showColumnsToggleSwitch.click(toggleColumns);
    staticModeToggleSwitch.click(toggleStaticMode);
}

$(document).ready(initialiseEventListeners);