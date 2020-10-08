function searchInputChanged() {
    freezeHighlight = false;
    unHighlightGraph();
    let searchText = searchInput.val();
    if (searchText === "") return;
    let fuzzyMatchedNodes = getNodesThatFuzzyMatchName(searchText);
    fuzzyMatchedNodes.map(node => node.id).forEach(id => highlightIds(getAllLineageSiblingIds(id)));
    freezeHighlight = true;
}

function getNodesThatFuzzyMatchName(fuzzyName) {
    return nodes.filter(node => node.name.includes(fuzzyName));
}