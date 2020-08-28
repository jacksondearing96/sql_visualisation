<template>
    <div id="forceGraph" class="text-center">
        <div class="jumbotron">
            <h3>Base table for {{ baseTable }} tables </h3>
            <h3>Derived from {{ derivedTable }} tables </h3>
            <svg>
                <defs>

                    <marker id="m-end" markerWidth="10" markerHeight="10" refX="20" refY="3" orient="auto"
                        markerUnits="strokeWidth">
                        <path d="M0,0 L0,6 L9,3 z"></path>
                    </marker>

                    <marker id="m-start" markerWidth="6" markerHeight="6" refX="-20" refY="3" orient="auto"
                        markerUnits="strokeWidth">
                        <rect width="4" height="6"></rect>
                    </marker>

                </defs>
            </svg>

            <d3-network :net-nodes="nodes" :net-links="links" :options="options" :link-cb="linkInfo"
                @node-click="nodeInfo" />
        </div>
    </div>
</template>

<script>
    import D3Network from 'vue-d3-network'
    export default {
        data() {
            return {
                nodes: [],
                links: [],
                nodeSize: 25,
                canvas: false,
                tableLinkInformation: [],
                baseTable: 0,
                derivedTable: 0
            }
        },
        computed: {
            options() {
                return {
                    nodeLabels: true,
                    size: {
                        w: 1000,
                        h: 500
                    },
                    nodeSize: this.nodeSize,
                    force: 5000,
                    canvas: this.canvas
                }
            }
        },
        methods: {
            linkInfo(link) {
                if (link.index != null) this.tableLinkInformation.push(link)

                link._svgAttrs = {
                    'marker-end': 'url(#m-end)',
                    'marker-start': 'url(#m-start)'
                }
                return link
            },
            nodeInfo(event, node) {
                this.baseTable = 0
                this.derivedTable = 0


                this.tableLinkInformation.forEach(element => {
                    if (element.sid == node.id) {
                        this.baseTable += 1
                    }

                    if (element.tid == node.id) {
                        this.derivedTable += 1
                    }
                })
            }
        },
        created() {
            // Retrieving all nodes from vuex
            this.nodes = this.$store.getters.allNodes

            // Retrieving all links from vuex
            this.links = this.$store.getters.allLinks
        },
        components: {
            D3Network
        }
    }
</script>

<style>

</style>