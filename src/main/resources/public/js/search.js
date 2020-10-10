/* Given the name of the item in the graph, match the node and draw the lineage */
function highlightLineage(itemName) {
    freezeHighlight = false;
    unHighlightGraph();
    let matchingNodes = nodes.filter(function (node) { return node.name === itemName;});
    matchingNodes.map(node => node.id).forEach(id => highlightIds(getAllLineageSiblingIds(id)));
    freezeHighlight = true;
}

/* The autocomplete function takes a search input and array of search terms. */
function autocomplete(input, arrayOfSearchTerms) {
    var currentFocus;

    /* Execute the function when someone writes in the text field */
    input.addEventListener("input", getAllPossibleMatches)
    input.addEventListener("keydown", navigateOptions)

    /* Dump the search fields if someone clicks elsewhere */
    document.addEventListener("click", function (e) {
        closeAllLists(e.target);
    });
}

/* Remove objects with matching keys */
function getUniqueListBy(arr, key) {
    return [...new Map(arr.map(item => [item[key], item])).values()]
}

/* Returns a list of possible autocompleted values */
function getAllPossibleMatches(e) {
    var a, b, i, val = this.value;

    /* Close autocompleted options, clear highlighting, return to mouse control */
    closeAllLists();
    if (!val) {
        freezeHighlight = false;
        unHighlightGraph();
        return false;
    }

    currentFocus = -1;

    /* Create a DIV element which will contain the matching values */
    elem = document.createElement("DIV")
    elem.setAttribute("id", this.id + "autocomplete-list")
    elem.setAttribute("class", "autocomplete-items")

    /* Append the DIV element as a child of the autocomplete container */
    this.parentNode.appendChild(elem)

    /* Remove duplicate node names */
    searchNodes = getUniqueListBy(nodes, "name")

    /* Get all the matching fields */
    let searchValues = searchNodes.map(node => node.name)
    let searchResult = fuzzysort.go(val, searchValues)

    /* Sort by element score */
    searchResult.sort((a, b) => (a.score - b.score));

    /* When we have not *great* search results, only return 3 *best* matches */
    for (i = 0; i < Math.min(searchResult.length, 10); i++) {
        /* Create a DIV element for each matching element */
        optElem = document.createElement("DIV")

        /* Bold every element in the string which is matching */
        optElem.innerHTML = ""
        for (j = 0; j < searchResult[i].target.length; j++) {
            if (val.toLowerCase().includes(searchResult[i].target[j].toLowerCase())) {
                optElem.innerHTML += "<strong>" + searchResult[i].target[j] + "</strong>"
            } else {
                optElem.innerHTML += searchResult[i].target[j]
            }
        }
        /* Insert a input field that will hold the current array item's value */
        optElem.innerHTML += "<input type='hidden' value='" + searchResult[i].target + "'>";

        /* Insert the value for the autocomplete text field: */
        optElem.addEventListener("click", function(e) {
            document.getElementById("search-input").value = this.getElementsByTagName("input")[0].value;
            closeAllLists();
            highlightLineage(this.getElementsByTagName("input")[0].value)
        });
        elem.append(optElem);
    }
}

/* Move between fields with arrow keys */
function navigateOptions(e) {
    var x = document.getElementById(this.id + "autocomplete-list");
    if (x) x = x.getElementsByTagName("div");
    if (e.keyCode == 40) {
        currentFocus++;
        addActive(x);
    } else if (e.keyCode == 38) {
        currentFocus--;
        addActive(x);
    } else if (e.keyCode == 13) {
        e.preventDefault();
        if (currentFocus > -1) {
          if (x) x[currentFocus].click();
        }
    }
}

/* Highlight an autocompleted field */
function addActive(x) {
    if (!x) return false;
    removeActive(x);
    if (currentFocus >= x.length) currentFocus = 0;
    if (currentFocus < 0) currentFocus = (x.length - 1);
    x[currentFocus].classList.add("autocomplete-active");
}

/* Remove a highlighted autocomplete field */
function removeActive(x) {
    for (var i = 0; i < x.length; i++) {
        x[i].classList.remove("autocomplete-active");
    }
}

/* Closes all the autocompleted lists in the document, except the one parsed as the argument */
function closeAllLists(elem) {
    var x = document.getElementsByClassName("autocomplete-items");
    for (var i = 0; i < x.length; i++) {
        if (elem != x[i] && document.getElementById("search-input").value != x[i]) {
            x[i].parentNode.removeChild(x[i]);
        }
    }
}