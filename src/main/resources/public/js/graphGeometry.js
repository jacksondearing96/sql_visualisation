function calculateTextWidth(text) {
    let numberOfCharacters = text.length;
    let width = fontSize * fontSizeToCharacterWidthRatio * numberOfCharacters;
    return width;
}

function calculateTextWidthWithPadding(text) {
    return calculateTextWidth(text) + 2 * labelPaddingHorizontal;
}

Array.prototype.max = function() {
    return Math.max.apply(null, this);
};

function maxColumnWidthForGroup(group) {
    return nodes
        .filter((node) => node.group === group)
        .filter((node) => node.type === columnType)
        .map((node) => calculateTextWidthWithPadding(node.name))
        .max();
}

function topLevelWidthForGroup(group) {
    return calculateTextWidthWithPadding(
        nodes.filter((node) => isTopLevelNode(node) && node.group === group)[0].name
    );
}

function calculateNodeWidth(node) {
    let maxColumnWidth = maxColumnWidthForGroup(node.group);
    let topLevelWidth = topLevelWidthForGroup(node.group);

    if (isTopLevelNode(node)) {
        return Math.max(
            maxColumnWidth + 2 * topLevelNodePaddingHorizontal,
            calculateTextWidthWithPadding(node.name)
        );
    }
    return Math.max(maxColumnWidth, topLevelWidth - 2 * topLevelNodePaddingHorizontal);
}

function calculateNodeHeight(node) {
    return isTopLevelNode(node) ?
        columnHeight * countColumnsInGroup(node.group) + topLevelNodePaddingVertical :
        columnHeight;
}


function getNodeX(node) {
    if (isTopLevelNode(node)) return node.x;
    return getNodeX(getParentTable(node)) + topLevelNodePaddingHorizontal;
}

function getNodeY(node) {
    if (isTopLevelNode(node)) return node.y;
    let parentY = getNodeY(getParentTable(node));
    return (
        parentY + parseInt(node.order, 10) * columnHeight + topLevelNodePaddingVertical
    );
}

function getLabelX(node) {
    return getNodeX(node) + labelPaddingHorizontal;
}

function getLabelY(node) {
    if (isTopLevelNode(node)) return getNodeY(node) + columnHeight / 2 + labelOffsetToReachCenter + 5;
    return getNodeY(node) + columnHeight / 2 + labelOffsetToReachCenter;
}

function getLinkSourceX(link) {
    if (isTopLevelId(link.source.id)) {
        return (
            getNodeX(link.source) + calculateTextWidthWithPadding(link.source.name)
        );
    }
    return getNodeX(link.source) + calculateNodeWidth(link.source);
}

function getLinkSourceY(link) {
    if (isTopLevelId(link.source.id)) {
        return getNodeY(link.source) + calculateNodeHeight(link.source) / 2;
    }
    return getNodeY(link.source) + columnHeight / 2;
}

function getLinkTargetX(link) {
    return getNodeX(link.target);
}

function getLinkTargetY(link) {
    if (isTopLevelId(link.target.id)) {
        return getNodeY(link.target) + calculateNodeHeight(link.target) / 2;
    }
    return getNodeY(link.target) + columnHeight / 2;
}

function ticked() {
    nodeSelection.attr('x', (d) => getNodeX(d)).attr('y', (d) => getNodeY(d));

    lables.attr('x', (d) => getLabelX(d)).attr('y', (d) => getLabelY(d));

    linkSelection
        .attr('x1', (d) => getLinkSourceX(d))
        .attr('y1', (d) => getLinkSourceY(d))
        .attr('x2', (d) => getLinkTargetX(d))
        .attr('y2', (d) => getLinkTargetY(d));
}