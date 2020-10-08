let sql = '';

function isValidSqlFileContents(contents) {
    let isValid = false;
    return new Promise(resolve => {
        $.post("/verify_sql", contents, isValid => {
            console.log(isValid);
            resolve(isValid === 'true');
        });
    });
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
        reader.onload = async function(event) {
            let isValid = await isValidSqlFileContents(event.target.result);
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
    let readFilePromises = sortFiles(files).map(readFile);

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