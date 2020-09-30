let NOT_FOUND = -1;

let uploadFilesInput = $('#upload-files-input');
let generateVisualisationButton = $('#generate-visualisation-button');
let demoButton = $('#demo-button');
let chooseFilesButton = $('#choose-files-button');
let fileListContainer = $('#file-list-container');
let fileNameContainers = $('.file-name-container');

let tickImg = "<img class='tick' src='https://lesspestcontrol.com.au/wp-content/uploads/green-tick.png'>";
let crossImg = "<img class='cross' src='https://freesvg.org/img/milker_X_icon.png'>";

const propic_file_order = [
    'crm_join.sql',
    'supporting_views.sql',
    'appraisal_case.sql',
    'buyer_vendor_case.sql',
    'record_sale_nearby.sql',
    'leads_from_crm.sql',
    'leads_from_market.sql',
    'leads_with_score.sql',
    'agent_leads.sql'
]

let sql = '';

function isValidSqlFileContents(contents) {
    return true;
}

function fileLine(img, filename) {
    return '<div class="file-name-container">' + img + '<p>' + filename + '</p></div><br>';
}

function validFile(filename) {
    return fileLine(tickImg, filename);
}

function invalidFile(filename) {
    return fileLine(crossImg, filename);
}

function readFile(file) {
    return new Promise(resolve => {
        let reader = new FileReader();
        reader.readAsText(file, 'UTF-8');
        reader.onload = event => {
            let isValid = isValidSqlFileContents(event.target.result);
            let listItem = isValid ? validFile(file.name) : invalidFile(file.name);
            let contentsAndListItem = { contents: event.target.result, listItem: listItem };
            resolve(contentsAndListItem);
        }
    });
}

/**
 * Returns the sorting order for a filename based on the predefined Propic sorting order.
 * All files not defined in Propic's list will be ordered randomly at the end of the ordered list.
 */
function sortingOrder(file) {
    let preSortedFileIndex = propic_file_order.indexOf(file.name);
    return (preSortedFileIndex === NOT_FOUND) ? propic_file_order.length : preSortedFileIndex;
}

function sortFiles(files) {
    let sortedFiles = [];
    for (let file of files) {
        sortedFiles.push(file);
    }
    return sortedFiles.sort((file1, file2) => sortingOrder(file1) - sortingOrder(file2));
}

function errorFileExists() {
    return fileListContainer.find('img.cross').length != 0
}

function uploadFiles() {
    // Clear any files currently uploaded and the sql.
    fileListContainer.html('');
    sql = '';

    // Read files asynchronously.
    let files = document.getElementById('upload-files-input').files;
    let readFilePromises = sortFiles(files).map(file => readFile(file));

    Promise.all(readFilePromises).then(contentsAndListItems => {
        fileListContainer.show();
        contentsAndListItems.forEach(contentsAndListItem => {
            fileListContainer.append(contentsAndListItem.listItem);
            sql += contentsAndListItem.contents;
        });

        // Activate button if all files are validated.
        if (!errorFileExists()) generateVisualisationButton.prop('disabled', false);
    });
}

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