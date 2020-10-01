function determineNodeColor(node) {
    return isTopLevelNode(node) ?
        topLevelNodeDefaultBackgroundColor :
        columnDefaultBackgroundColor;
}

function determineNodeOpacity(node) {
    return isTopLevelNode(node) ? topLevelNodeDefaultOpacity : columnDefaultOpacity;
}

function determineTextColor(node) {
    return isTopLevelNode(node) ? topLevelNodeDefaultTextColor : columnDefaultTextColor;
}