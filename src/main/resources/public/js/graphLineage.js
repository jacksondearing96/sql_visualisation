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
    }

    let sourceSiblings = [];
    sourceSiblings.push(
        ...[id, ...sourceColumnIds, ...column.incoming.map(link => link.id)]
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
    }

    let targetSiblings = [];
    targetSiblings.push(
        ...[id, ...targetColumnIds, ...column.outgoing.map(link => link.id)]
    );
    return targetSiblings;
}

function getAllLineageSiblingIds(id) {
    let siblingIds = [
        id,
        ...getAllSourceSiblings(id),
        ...getAllTargetSiblings(id)
    ];

    siblingIds = siblingIds.filter(
        (siblingId, index) => siblingIds.indexOf(siblingId) === index
    );

    return siblingIds;
}