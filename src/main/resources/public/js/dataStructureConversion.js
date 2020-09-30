function getGroupFromId(id) {
    let idParts = id.split("::");
    if (idParts.length < 1) error("Invalid id for node: " + id);
    return idParts[0];
}

function makeNode(type, name, id, order = null) {
    let node = {};
    node.type = type;
    node.name = name;
    node.id = id;
    if (type === "COLUMN") node.order = order;
    node.group = getGroupFromId(id);
    return node;
}

function makeLink(source, target, idCount) {
    let link = {};
    link.source = source;
    link.target = target;
    link.id = "link".concat(idCount);
    return link;
}

function addColumnToGraph(graph, column, order) {
    // Add the column as a node.
    graph.nodes.push(makeNode(column.type, column.name, column.id, order));

    // Add each of the sources as links.
    column.sources.forEach(source => graph.links.push(makeLink(source, column.id, graph.links.length)));
}

function backendToFrontendDataStructureConversion(lineageNodes) {

    let graph = {
        nodes: [],
        links: []
    }

    lineageNodes.forEach(lineageNode => {
        // Add the top level node.
        nodes.push(makeNode(lineageNode.type, lineageNode.name, lineageNode.id));

        // Add each of the columns.
        lineageNode.columns.forEach((column, index) => {
            addColumnToGraph(graph, column, index);
        });
    });

    return graph;
}