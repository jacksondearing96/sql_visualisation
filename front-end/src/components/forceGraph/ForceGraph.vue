<template>
    <div id="forceGraph" class="text-center">
        <!-- Force Graph -->
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
        <!-- End Force Graph -->

        <!-- Node info -->
        <div id="infoButton" class="container">
            <p class="lead mr-3">Node selected: {{ nodeSelected }}
                <button class="btn btn-info ml-4" data-bs-hover-animate="swing" type="button" @click="showColumns">Info</button>
            </p>
        </div>
        <!-- End Node info -->

        <!-- Table Info -->
        <TableInfo v-if="isColumn" :node="nodeSelected"></TableInfo>
        <!-- End Table Info -->
    </div>
</template>

<script>
    import D3Network from 'vue-d3-network'
    import TableInfo from '@/components/tableInfo/TableInfo.vue'
    export default {
        data() {
            return {
                nodes: [],
                links: [],
                nodeSize: 25,
                canvas: false,
                tableLinkInformation: [],
                baseTable: 0,
                derivedTable: 0,
                isColumn: false,
                nodeSelected: ''
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
            },
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
                this.nodeSelected = node.name
                this.isColumn = false
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
            },
            showColumns(){
                this.isColumn = true;
            }
        },
        created() {
            // Retrieving all nodes and links from vuex
            this.nodes = this.$store.getters.allNodes
            this.links = this.$store.getters.allLinks
        },
        components: {
            D3Network,
            TableInfo
        }
    }
</script>

<style>

</style>