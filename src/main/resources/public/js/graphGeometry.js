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

function topLevelNodes() {
    return nodes.filter(isTopLevelNode);
}

function setGridColumns(link) {
    let source = getNodeById(link.source.id);
    let target = getNodeById(link.target.id);

    if (source.gridColumn >= target.gridColumn) {
        target.gridColumn = source.gridColumn + 1;
    }
}

function getGridColumnWidth(columnIndex) {
    return nodesInGridColumn(columnIndex)
        .map(calculateNodeWidth)
        .max();
}

function maxGridColumnIndex() {
    return nodes
        .filter(isTopLevelNode)
        .map(node => node.gridColumn)
        .max();
}

function setGridStartingWidths() {
    for (let columnIndex = 1; columnIndex <= maxGridColumnIndex(); ++columnIndex) {
        let prevIndex = columnIndex - 1;
        gridStartingWidths[columnIndex] = gridStartingWidths[prevIndex] + getGridColumnWidth(prevIndex) + topLevelNodeWidthBuffer;
    }
}

function setGridVerticalOrders() {
    for (let columnIndex = 0; columnIndex <= maxGridColumnIndex(); ++columnIndex) {
        let verticalOrder = -1;
        nodes
            .filter(node => isTopLevelNode(node) && node.gridColumn === columnIndex)
            .forEach(node => {
                node.verticalOrder = ++verticalOrder;
            });
    }
}

function hasAtLeastOneLink(node) {
    if (!isTopLevelNode(node)) error('Testing independence of non-top-level node');

    let hasLink = false;
    generateSimplifiedGraph().links.forEach(link => {
        if (link.source.id === node.id || link.target.id === node.id) hasLink = true;
    });
    return hasLink;
}

function separateNonLinkedNodes() {
    topLevelNodes().filter(hasAtLeastOneLink).forEach(node => {
        ++node.gridColumn;
    });
}

function nodesInGridColumn(columnIndex) {
    return topLevelNodes().filter(node => node.gridColumn === columnIndex);
}

function maxVerticalOrderForGridColumn(columnIndex) {
    return nodesInGridColumn(columnIndex)
        .map(node => node.verticalOrder)
        .max();
}

function getGridKey(columnIndex, verticalOrder) {
    return columnIndex + "::" + verticalOrder;
}

function setGridStartingHeights() {
    let maxGridColumnIndex_ = maxGridColumnIndex();
    for (let columnIndex = 0; columnIndex <= maxGridColumnIndex_; ++columnIndex) {
        let maxVerticalOrder = maxVerticalOrderForGridColumn(columnIndex);
        for (let verticalOrder = 0; verticalOrder <= maxVerticalOrder; ++verticalOrder) {
            let height = 0;
            if (verticalOrder !== 0) {
                let prevNode = nodesInGridColumn(columnIndex)
                    .filter(node => node.verticalOrder === verticalOrder - 1)[0];
                height += gridStartingHeights[getGridKey(columnIndex, verticalOrder - 1)];
                height += calculateNodeHeight(prevNode);
                height += topLevelNodeHeightBuffer;
            }
            gridStartingHeights[getGridKey(columnIndex, verticalOrder)] = height;
        }
    }
}

function distance(x1, x2, y1, y2) {
    return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
}

function linkDistance(link) {
    return distance(getLinkSourceX(link), getLinkTargetX(link), getLinkSourceY(link), getLinkTargetY(link));
}

function totalLinksSquaredDistance() {
    return links.map(link => Math.pow(linkDistance(link), 2)).reduce((a, b) => a + b, 0);
}

function randomiseVerticalOrder(columnIndex) {
    let maxVerticalOrder = maxVerticalOrderForGridColumn(columnIndex);
    let verticalOptions = Array.from(Array(maxVerticalOrder + 1).keys());
    shuffleArray(verticalOptions);
    nodesInGridColumn(columnIndex).forEach(node => {
        node.verticalOrder = verticalOptions.pop();
    });
}


function randomShuffleColumn(columnIndex) {
    randomiseVerticalOrder(columnIndex);
    setGridStartingHeights();
    ticked();
}

function flagBestVerticalOrderForNode(node) {
    node.bestVerticalOrder = node.verticalOrder;
}

function flagBestVerticalOrderForAll() {
    topLevelNodes().forEach(flagBestVerticalOrderForNode);
}

function flagBestVerticalOrderForColumn(columnIndex) {
    nodesInGridColumn(columnIndex).forEach(flagBestVerticalOrderForNode);
}

function revertToBestVerticalOrder() {
    topLevelNodes().forEach(node => {
        node.verticalOrder = node.bestVerticalOrder;
    });
}

function optimiseTablePositions() {
    let minimisedLinkSquaredDistance = totalLinksSquaredDistance();
    flagBestVerticalOrderForAll();
    let startingDistance = minimisedLinkSquaredDistance;

    let numberOfColumns = maxGridColumnIndex() + 1;
    for (let i = 0; i < optimisationIterations; ++i) {
        for (let columnIndex = 1; columnIndex < numberOfColumns; ++columnIndex) {

            randomShuffleColumn(columnIndex);
            let linkSquaredDistance = totalLinksSquaredDistance();
            if (linkSquaredDistance < minimisedLinkSquaredDistance) {
                flagBestVerticalOrderForColumn(columnIndex);
                minimisedLinkSquaredDistance = linkSquaredDistance;
            }
        }
    }

    let proportionDecrease = 1.0 - (minimisedLinkSquaredDistance / startingDistance);
    let percentageDecrease = proportionDecrease * 100;
    console.log('Optimisation: ' + percentageDecrease.toFixed(2) + "%");

    revertToBestVerticalOrder();
    setGridStartingHeights();
    ticked();
}

function allocateInitialPositions() {
    let showColumnsSavedState = showColumns;
    // Ensure the top level nodes are the only ones that appear in the lineage extraction.
    showColumns = false;

    generateSimplifiedGraph().links.forEach(setGridColumns);
    separateNonLinkedNodes();
    setGridStartingWidths();

    setGridVerticalOrders();
    setGridStartingHeights();
    optimiseTablePositions();

    showColumns = showColumnsSavedState;
}

function getNodeX(node) {
    if (isTopLevelNode(node)) {
        return staticMode ?
            canvasWidth / 2 + gridStartingWidths[node.gridColumn] :
            node.x;
    }
    return getNodeX(getParentTable(node)) + topLevelNodePaddingHorizontal;
}

function getNodeY(node) {
    if (isTopLevelNode(node)) {
        return staticMode ?
            canvasHeight / 2 + gridStartingHeights[getGridKey(node.gridColumn, node.verticalOrder)] :
            node.y;
    }
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
        .attr('x', getNodeX)
        .attr('y', getNodeY);
    if (showColumnsChanged) nodeSelection.attr('height', calculateNodeHeight);

    labels
        .attr('x', getLabelX)
        .attr('y', getLabelY);

    linkSelection
        .attr('x1', getLinkSourceX)
        .attr('y1', getLinkSourceY)
        .attr('x2', getLinkTargetX)
        .attr('y2', getLinkTargetY);

    // Performance optimisation.
    showColumnsChanged = false;
}