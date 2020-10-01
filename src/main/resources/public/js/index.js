function generateVisualisationButtonClicked() {
    $.post("/lineage_extractor", sql, lineageNodes => {
        let graph = backendToFrontendDataStructureConversion(JSON.parse(lineageNodes));
        generateVisualisation(graph);
    });
}

function initialiseEventListeners() {
    uploadFilesInput.change(uploadFiles);
    chooseFilesButton.click(() => uploadFilesInput.trigger('click'));
    generateVisualisationButton.click(generateVisualisationButtonClicked);
    demoButton.click(() => generateVisualisation(demoGraph));
}

$(document).ready(() => initialiseEventListeners());