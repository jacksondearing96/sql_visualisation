function getNodeById(id) {
    for (let node of nodes) {
        if (node.id === id) return node;
    }
    error('Could not find node with id: ' + id);
}

function isTopLevelNode(node) {
    return node.type === tableType || node.type === viewType;
}

function isTopLevelId(id) {
    return id.endsWith(idDelimiter);
}

function countColumnsInGroup(group) {
    return nodes.filter((node) => node.group === group).length;
}

function getParentTable(node) {
    let parent = nodes.filter((n) => isTopLevelNode(n) && n.group === node.group);
    if (parent.length !== 1) error('Could not find parent table.');
    return parent[0];
}

function columnOfLabel(label) {
    if (!label.id.includes('label-')) error('Invalid label ID: ' + label.id);
    return label.id.split('label-')[1];
}

function setGroupClasses(node) {
    return node.group + ' ' + isTopLevelNode(node) ? topLevelNodeClass : columnClass;
}

function getAllChildColumnIdsFromTopLevelId(id) {
    return nodes
        .filter(
            (node) => node.group === getNodeById(id).group && node.type === columnType
        )
        .map((node) => node.id);
}