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
        .filter(node => node.group === group)
        .filter(node => node.type === columnType)
        .map(node => calculateTextWidthWithPadding(node.name))
        .max();
}

function topLevelWidthForGroup(group) {
    return calculateTextWidthWithPadding(
        nodes.filter(node => isTopLevelNode(node) && node.group === group)[0].name
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
    if (isTopLevelNode(node) && showColumns) {
        return columnHeight * countColumnsInGroup(node.group) + topLevelNodePaddingVertical;
    }
    return isTopLevelNode(node) ? topLevelNodeCollapsedHeight : columnHeight;
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

function getLinkSource(link) {
    return isTopLevelNode(link.source) ? link.source :
        showColumns ? link.source : getParentTable(link.source);
}

function getLinkTarget(link) {
    return isTopLevelNode(link.target) ? link.target :
        showColumns ? link.target : getParentTable(link.target);
}

function getLinkSourceX(link) {
    let linkSource = getLinkSource(link);
    return getNodeX(linkSource) + calculateNodeWidth(linkSource);
}

function getLinkSourceY(link) {
    let linkSource = getLinkSource(link);
    return isTopLevelNode(linkSource) ?
        getNodeY(linkSource) + calculateNodeHeight(linkSource) / 2 :
        getNodeY(linkSource) + columnHeight / 2;
}

function getLinkTargetX(link) {
    return getNodeX(getLinkTarget(link));
}

function getLinkTargetY(link) {
    let linkTarget = getLinkTarget(link);
    return isTopLevelId(linkTarget.id) ?
        getNodeY(linkTarget) + calculateNodeHeight(linkTarget) / 2 :
        getNodeY(linkTarget) + columnHeight / 2;
}

function ticked() {
    nodeSelection
        .attr('x', node => getNodeX(node))
        .attr('y', (node) => getNodeY(node));
    if (showColumnsChanged) nodeSelection.attr('height', node => calculateNodeHeight(node));

    labels
        .attr('x', label => getLabelX(label))
        .attr('y', label => getLabelY(label));

    linkSelection
        .attr('x1', link => getLinkSourceX(link))
        .attr('y1', link => getLinkSourceY(link))
        .attr('x2', link => getLinkTargetX(link))
        .attr('y2', link => getLinkTargetY(link));

    // Performance optimisation.
    showColumnsChanged = false;
}