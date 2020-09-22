var nodes = [
  { color: "grey", group: "1", tag: "table" },
  { color: "red", group: "1", order: "0", tag: "column" },
  { color: "orange", group: "1", order: "1", tag: "column" },
  { color: "green", group: "1", order: "2", tag: "column" },

  { color: "grey", group: "3", tag: "table" },
  { color: "yellow", group: "3", order: "0", tag: "column" },

  { color: "grey", group: "2", tag: "table" },
  { color: "blue", group: "2", order: "0", tag: "column" },
  { color: "purple", group: "2", order: "1", tag: "column" }
];

var links = [
  { source: "yellow", target: "red", tag: "" },
  { source: "green", target: "blue", tag: "" }
];