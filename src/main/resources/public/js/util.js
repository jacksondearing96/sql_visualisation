function error(message) {
    console.error(message);
    throw new Error(message);
}

let logCount = 0;

function log(message) {
    if (logCount < loggingCountThreshold) console.log(message);
    if (logCount === loggingCountThreshold) console.error('Logging capacity exceeded!');
    ++logCount;
}

function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
}