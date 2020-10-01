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

/////////////////////////////////////////////////////////////////////////////////
// TODO: Use the dedicated function for this.
function linkMouseOut(link) {
    unHighlightIds([
        link.id,
        ...getAllSourceSiblings(link.source.id),
        ...getAllTargetSiblings(link.target.id)
    ]);
}