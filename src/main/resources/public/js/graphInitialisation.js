var nodes = []
var links = []
var simulation, nodeSelection, linkSelection, labels, svg;

function generateVisualisation(graph) {
    initialiseContainer();
    initialiseGraphData(graph);
    generateForceDirectedSimulation();
    activateToggleColumnsButton();
}

function initialiseContainer() {
    $('#visualisation-container').show(600, initialiseScrollPosition);
}

function initialiseScrollPosition() {
    $('#visualisation-container').scrollTop(canvasHeight / 2 + scrollIncrementHeightToInitInCenter);
    $('#visualisation-container').scrollLeft(canvasWidth / 2 + scrollIncrementWidthToInitInCenter);
}

function initialiseGraphData(graph) {
    nodes = graph.nodes;
    links = graph.links;
    allocateIncomingAndOutgoingLinks();
}

function allocateIncomingAndOutgoingLinks() {
    links.forEach(link => {
        getNodeById(link.source).outgoing.push(link);
        getNodeById(link.target).incoming.push(link);
    });
}

function generateForceDirectedSimulation() {

    svg = d3
        .select('svg')
        .attr('width', canvasWidth)
        .attr('height', canvasHeight)
        .call(d3.zoom().on("zoom", function() {
            svg.attr("transform", d3.event.transform)
        }))
        .append("g");

    nodeSelection = svg
        .selectAll('rect')
        .data(nodes)
        .enter()
        .append('g')
        .attr('class', setNodeClass)
        .append('rect')
        .attr('class', node => node.group)
        .attr('width', calculateNodeWidth)
        .attr('height', calculateNodeHeight)
        .attr('fill', determineNodeColor)
        .attr('opacity', determineNodeOpacity)
        .attr('class', setNodeClass)
        .attr('id', node => node.id)
        .call(
            d3.drag()
            .on('start', dragStart)
            .on('drag', drag)
            .on('end', dragEnd))
        .on('mouseover', node => columnMouseOver(node.id))
        .on('mouseout', node => columnMouseOut(node.id));

    linkSelection = svg
        .selectAll('line')
        .data(links)
        .enter()
        .append('line')
        .attr('stroke', linkDefaultColor)
        .attr('fill', linkFill)
        .attr('stroke-width', linkDefaultWidth)
        .attr('id', link => link.id)
        .attr('class', 'link')
        .on('mouseover', linkMouseOver)
        .on('mouseout', linkMouseOut);

    labels = svg
        .selectAll('g')
        .append('text')
        .attr('fill', determineTextColor)
        .attr('font-size', fontSize)
        .attr('font-family', fontFamily)
        .attr('font-weight', label =>
            isTopLevelNode(label) ? topLevelNodeDefaultFontWeight : columnFontWeight
        )
        .attr('class', 'label')
        .attr('id', label => 'label-' + label.id)
        .text(label => label.name)
        .on('mouseover', labelMouseOver)
        .on('mouseout', labelMouseOut)
        .call(
            d3.drag()
            .on('start', dragStart)
            .on('drag', drag)
            .on('end', dragEnd));

    simulation = d3.forceSimulation(nodes);

    simulation
        .force('center', d3.forceCenter(canvasWidth / 2, canvasHeight / 2))
        .force('collision', d3.forceCollide(d => calculateNodeWidth(d) * collisionWidthRatio / 2)).force(
            'links',
            d3
            .forceLink(links)
            .id(link => link.id)
            .distance(linkPreferredDistance)
            .strength(linkForceStrength)
        )
        .on('tick', ticked);
}