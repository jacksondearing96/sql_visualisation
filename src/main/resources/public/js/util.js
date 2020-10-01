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