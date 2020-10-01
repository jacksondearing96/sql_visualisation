function dragStart(d) {
    simulation.alphaTarget(dragStartAlphaTarget).restart();
    d.fx = d.x;
    d.fy = d.y;
}

function drag(d) {
    d.fx = d3.event.x;
    d.fy = d3.event.y;
}

function dragEnd(d) {
    simulation.alphaTarget(dragEndAlphaTarget);
    d.fx = null;
    d.fy = null;
}