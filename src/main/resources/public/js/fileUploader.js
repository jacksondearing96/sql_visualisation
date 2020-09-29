let tickImg = "<img class='tick' src='https://lesspestcontrol.com.au/wp-content/uploads/green-tick.png'>";
let crossImg = "<img class='cross' src='https://freesvg.org/img/milker_X_icon.png'>";

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
    reader.readAsText(file, "UTF-8");
    reader.onload = event => {
        let fileContents = event.target.result;
        let isValid = isValidSqlFileContents(fileContents);
        let listItem = isValid ? validFile(file.name) : invalidFile(file.name);
        $('#file-list').append(listItem);
        resolve(isValid)
    }
  });
}

function uploadFiles() {
    let files = document.getElementById('upload-files-button').files;
    
    let readFilePromises = [];
    for (let file of files) {
      readFilePromises.push(readFile(file));
    }

    Promise.all(readFilePromises).then((isValidList) => {
      if (!isValidList.includes(false)) {
        $("#file-upload-container button").prop('disabled', false);
      }
    });
}

function generateVisualisation() {
  $('#visualisation-container').show();
}

$(document).ready(() => {
  $('#upload-files-button').change(uploadFiles);
  $('#file-upload-container button').click(generateVisualisation);
});