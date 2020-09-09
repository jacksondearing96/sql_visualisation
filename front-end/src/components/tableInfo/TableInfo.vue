<template>
    <div class="tableInfo">
        <div class="tableColumn-boxed">
            <div class="container">
                <div class="intro">
                    <h2 class="text-center">Table Name: {{ node }}</h2>
                </div>
                <div class="row tColumn">
                    <div class="col-md-6 col-lg-4 item" v-for="(column, index) in columns" :key="index">
                        <div class="box">
                            <h3 class="name">{{ column.name }}</h3>
                            <p class="alias">{{ column.alias }}</p>
                            <div class="sources">
                                <span>Sources:</span>
                                <p class="mt-2" v-for="(source,index) in column.sources" :key="index">{{ source }}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import {mapGetters} from 'vuex'
    export default {
        name: 'TableInfo',
        props: {
            node: {
                type: String
            }
        },
        data(){
            return {
                columns: []
            }
        },
        computed: {
            ...mapGetters(['allTables'])
        },
        created(){
            this.allTables.forEach(element => {
                if(this.node == element.name) this.columns = element.columns
            });
        }
    }
</script>

<style scoped>
    .tableInfo {
        background-color: rgb(255, 255, 255);
    }

    .tableColumn-boxed {
        color: #313437;
        background-color: #ffffff;
    }

    .tableColumn-boxed p {
        color: #7d8285;
    }

    .tableColumn-boxed h2 {
        font-weight: bold;
        margin-bottom: 40px;
        padding-top: 40px;
        color: inherit;
    }

    @media (max-width:767px) {
        .tableColumn-boxed h2 {
            margin-bottom: 25px;
            padding-top: 25px;
            font-size: 24px;
        }
    }

    .tableColumn-boxed .intro {
        font-size: 16px;
        max-width: 500px;
        margin: 0 auto;
    }

    .tableColumn-boxed .intro p {
        margin-bottom: 0;
    }

    .tableColumn-boxed .tColumn {
        padding: 50px 0;
    }

    .tableColumn-boxed .item {
        text-align: center;
    }

    .tableColumn-boxed .item .box {
        text-align: center;
        padding: 30px;
        background-color: #fff;
        margin-bottom: 30px;
        border-width: 2px;
        border-style: solid;
    }

    .tableColumn-boxed .item .name {
        font-weight: bold;
        margin-top: 28px;
        margin-bottom: 8px;
        color: inherit;
        font-size: 30px;
    }

    .tableColumn-boxed .item .alias {
        text-transform: uppercase;
        font-weight: bold;
        color: #d0d0d0;
        letter-spacing: 2px;
        font-size: 13px;
    }

    .tableColumn-boxed .item .sources {
        font-size: 15px;
        margin-top: 15px;
        margin-bottom: 20px;
    }
</style>