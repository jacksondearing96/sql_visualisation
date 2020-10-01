let correctionX = 0;
let correctionY = 0;

function dragStart(node) {
    node = getParentTable(node);

    simulation.alphaTarget(dragStartAlphaTarget).restart();

    node.fx = node.x;
    node.fy = node.y;

    correctionX = 0;
    correctionY = 0;
}

function drag(node) {
    let parent = getParentTable(node);

    if (correctionX === 0 && correctionY === 0) {
        correctionX = parent.fx - node.x;
        correctionY = parent.fy - node.y;
    }

    parent.fx = d3.event.x + correctionX;
    parent.fy = d3.event.y + correctionY;
}

function dragEnd(node) {
    node = getParentTable(node);
    simulation.alphaTarget(dragEndAlphaTarget);

    node.fx = null;
    node.fy = null;
}