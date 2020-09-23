function getGroupFromId(id) {
  let idParts = id.split("::");
  if (idParts.length < 1) error("Invalid id for node: " + id);
  return idParts[0];
}

function makeNode(type, name, id) {
  let node = {};
  node.type = type;
  node.name = name;
  node.id = id;
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

function backendToFrontendDataStructureConversion(lineageNodes) {
  let nodes = [];
  let links = [];
  let linkCount = -1;

  for (let lineageNode of lineageNodes) {
    nodes.push(makeNode(lineageNode.type, lineageNode.name, lineageNode.id));

    for (let column of lineageNode.columns) {
      nodes.push(makeNode(column.type, column.name, column.id));

      for (let source of column.sources) {
        links.push(makeLink(source, column.id, ++linkCount))
      }
    }
  }

  return { nodes: nodes, links: links };
}