function getAllSourceSiblings(id) {
    let column = getNodeById(id);

    let sourceColumnIds = [];
    for (let incomingLink of column.incoming) {
        sourceColumnIds.push(...getAllSourceSiblings(incomingLink.source.id));
    }
    if (isTopLevelId(id)) {
        let allColumnsIds = getAllChildColumnIdsFromTopLevelId(id);
        for (let columnId of allColumnsIds) {
            sourceColumnIds.push(...getAllSourceSiblings(columnId));
        }
    } else {
        if (!showColumns) sourceColumnIds.push(getParentTable(getNodeById(id)).id);
    }

    let sourceSiblings = [];
    sourceSiblings.push(
        ...[...sourceColumnIds, ...column.incoming.map(link => link.id), id]
    );
    return sourceSiblings;
}

function getAllTargetSiblings(id) {
    let column = getNodeById(id);

    let targetColumnIds = [];
    for (let outgoingLink of column.outgoing) {
        targetColumnIds.push(...getAllTargetSiblings(outgoingLink.target.id));
    }
    if (isTopLevelId(id)) {
        let allColumnsIds = getAllChildColumnIdsFromTopLevelId(id);
        for (let columnId of allColumnsIds) {
            targetColumnIds.push(...getAllTargetSiblings(columnId));
        }
    } else {
        if (!showColumns) targetColumnIds.push(getParentTable(getNodeById(id)).id);
    }

    let targetSiblings = [];
    targetSiblings.push(
        ...[id, ...targetColumnIds, ...column.outgoing.map(link => link.id)]
    );
    return targetSiblings;
}

function getAllLineageSiblingIds(id) {
    let siblingIds = [
        ...getAllSourceSiblings(id),
        id,
        ...getAllTargetSiblings(id)
    ];

    siblingIds = siblingIds.filter(
        (siblingId, index) => siblingIds.indexOf(siblingId) === index
    );

    return siblingIds;
}

function topLevelLineageTargets(node) {

}

function topLevelLineageSources(node) {

}

function topLevelLineageLine(node) {
    if (!isTopLevelNode(node)) node = getParentTable(node.id);
}

function highlightColumns(columns) {
    if (freezeHighlight) return;
    $(columns).attr({
        fill: columnHighlightBackgroundColor,
    });
}

function highlightTopLevelNodes(nodes) {
    if (freezeHighlight) return;
    $(nodes).attr({
        fill: topLevelNodeBackgroundHighlightColor,
        opacity: topLevelNodeHighlightOpacity
    });
}

function highlightLabels(labels) {
    if (freezeHighlight) return;
    $(labels).attr('fill', labelHighlightTextColor);
}

function highlightLinks(links) {
    if (freezeHighlight) return;
    $(links).attr({
        stroke: linkHighlightColor,
        fill: linkHighlightColor,
        'stroke-width': linkHighlightWidth
    });
}

function unHighlightGraph() {
    nodes.forEach(node => unHighlightIds(node.id));
    links.forEach(link => unHighlightIds(link.id));
}

function unHighlightColumns(columns) {
    if (freezeHighlight) return;
    columns.attr('fill', columnDefaultBackgroundColor);
}

function unHighlightTopLevelNodes(nodes) {
    if (freezeHighlight) return;
    nodes
        .attr('fill', topLevelNodeDefaultBackgroundColor)
        .attr('opacity', topLevelNodeDefaultOpacity);
}

function unHighlightLabels(labels) {
    if (freezeHighlight) return;
    $(labels)
        .filter((index, label) => isTopLevelId(columnOfLabel(label)))
        .attr('fill', topLevelNodeDefaultTextColor);
}

function unHighlightLinks(links) {
    if (freezeHighlight) return;
    $(links).attr({
        stroke: linkDefaultColor,
        fill: linkDefaultColor,
        'stroke-width': linkDefaultWidth
    });
}

function highlightIds(ids) {
    highlightColumns(
        $('rect').filter((index, column) => !isTopLevelId(column.id) && ids.includes(column.id))
    );

    highlightTopLevelNodes(
        $('rect').filter((index, node) => isTopLevelId(node.id) && ids.includes(node.id))
    );

    highlightLabels(
        $('.label').filter((index, label) => ids.includes(columnOfLabel(label)))
    );

    highlightLinks($('.link').filter((index, link) => ids.includes(link.id)));
}

function unHighlightIds(ids) {
    unHighlightColumns(
        $('rect').filter((index, column) => !isTopLevelId(column.id) && ids.includes(column.id))
    );

    unHighlightTopLevelNodes(
        $('rect').filter((index, node) => isTopLevelId(node.id) && ids.includes(node.id))
    );

    unHighlightLabels(
        $('.label').filter((index, label) => ids.includes(columnOfLabel(label)))
    );

    unHighlightLinks($('.link').filter((index, link) => ids.includes(link.id)));
}

function columnMouseOver(id) {
    highlightIds(getAllLineageSiblingIds(id));
}

function columnMouseOut(id) {
    unHighlightIds(getAllLineageSiblingIds(id));
}

function labelMouseOver() {
    let columnId = $(this.parentElement).find('rect').attr('id');
    columnMouseOver(columnId);
}

function labelMouseOut() {
    let columnId = $(this.parentElement).find('rect').attr('id');
    columnMouseOut(columnId);
}

function linkMouseOver(link) {
    highlightIds([
        link.id,
        ...getAllSourceSiblings(link.source.id),
        ...getAllTargetSiblings(link.target.id)
    ]);
}

function linkMouseOut(link) {
    unHighlightIds([
        link.id,
        ...getAllSourceSiblings(link.source.id),
        ...getAllTargetSiblings(link.target.id)
    ]);
}