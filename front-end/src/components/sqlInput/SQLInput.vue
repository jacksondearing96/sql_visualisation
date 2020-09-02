<template>
    <div id="sqlinput" class="container highlight-clean">
        <div class="intro">
            <h2 class="text-center">Data Lineage Tool</h2>
            <p class="text-center">Nunc luctus in metus eget fringilla. Aliquam sed justo ligula. Vestibulum nibh erat,
                pellentesque ut laoreet vitae. </p>
        </div>
        <div class="buttons">
            <!-- <input class="form-control-lg" type="text" style="height: 40px;" placeholder="SQL script"> -->
            <div class="custom-file">
                <input ref="file" type="file" class="custom-file-input" id="inputFile" @change="extractFile">
                <label class="custom-file-label" for="inputFile" aria-describedby="inputFile">SQL script</label>
            </div>
            <button class="btn btn-primary" type="button" @click.prevent="upload">Upload!</button>
            <button class="btn btn-primary" type="button" @click.prevent="javaTest">java!</button>
        </div>
    </div>
</template>
<script>
    import {mapActions} from 'vuex'
    import axios from 'axios'
    export default {
        name: 'SQLInput',
        data(){
            return {
                file: ''
            }
        },
        methods: {
            ...mapActions(['uploadScript']),
            extractFile(){
                this.file = this.$refs.file.files[0]
            },
            upload(){
                // Instantiate formData object
                let formData = new FormData()

                // Append file to  formData
                formData.append('file', this.file)
                
                this.uploadScript(formData)
            },
            javaTest(){
                axios.post('http://127.0.0.1:5000/javatest', {
                    name: 'Gabriel'
                }).then(response => {
                    console.log(response)
                }).catch(error => {
                    console.log('java error' + error)
                })
            }
        }
    }
</script>
<style scoped>
    #sqlinput {
        margin-top: 120px;
    }

    .highlight-clean {
        color: #313437;
        background-color: #fff;
        padding: 50px 0;
    }

    .highlight-clean p {
        color: #7d8285;
    }

    .highlight-clean h2 {
        font-weight: bold;
        margin-bottom: 25px;
        line-height: 1.5;
        padding-top: 0;
        margin-top: 0;
        color: inherit;
    }

    .highlight-clean .intro {
        font-size: 16px;
        max-width: 500px;
        margin: 0 auto 25px;
    }

    .highlight-clean .buttons {
        font-family: Roboto, sans-serif;
        margin-left: 10px;
        text-align: center;
    }

    .highlight-clean .buttons .btn {
        padding: 16px 32px;
        margin: 6px;
        border: none;
        background: none;
        box-shadow: none;
        text-shadow: none;
        opacity: 0.9;
        text-transform: uppercase;
        font-weight: bold;
        font-size: 13px;
        letter-spacing: 0.4px;
        line-height: 1;
        outline: none;
        background-color: #ddd;
    }

    .highlight-clean .buttons .btn:hover {
        opacity: 1;
    }

    .highlight-clean .buttons .btn:active {
        transform: translateY(1px);
    }

    .highlight-clean .buttons .btn-primary {
        background-color: #055ada;
        color: #fff;
    }
</style>