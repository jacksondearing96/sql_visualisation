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