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
                <input ref="file" type="file" class="custom-file-input" id="inputFile" @change="extractFile" multiple>
                <label class="custom-file-label" for="inputFile" aria-describedby="inputFile">SQL script</label>
            </div>
            <button class="btn btn-primary" type="button" @click.prevent="upload">Upload!</button>
        </div>
    </div>
</template>
<script>
    import {
        mapActions
    } from 'vuex'
    import _ from 'lodash'
    export default {
        name: 'SQLInput',
        data() {
            return {
                files: '',
                text: ''
            }
        },
        methods: {
            ...mapActions(['uploadScript']),
            extractFile() {
                // Assigning file(s) to this.files
                this.files = this.$refs.file.files

                // Propic's file compilation order
                const order = [
                    'crm_join.sql',
                    'supporting_views.sql',
                    'appraisal_case.sql',
                    'buyer_vendor_case.sql',
                    'record_sale_nearby.sql',
                    'leads_from_crm.sql',
                    'leads_from_market.sql',
                    'leads_with_score.sql',
                    'agent_leads.sql'
                ]

                // Sorting files and assigning to this.files
                this.files = _.sortBy(this.files, obj => {
                    var i = 0
                    if (_.indexOf(order, obj.name) == -1) {
                        i++
                        return this.files.length + i
                    } else {
                        return _.indexOf(order, obj.name)
                    }
                })

                // Iterating over files, reading them and appending result to text
                this.files.forEach(file => {
                    const reader = new FileReader()

                    reader.onload = () => {
                        this.text += reader.result + '\n'
                    }

                    // This is asynchronous, that's why we need the onload above
                    reader.readAsText(file)
                })
            },
            upload() {
                // Instantiate formData object
                let formData = new FormData()

                // Append file(s) to  formData
                this.files.forEach((file, index) => {
                    formData.append('file[' + index + ']', file)
                })

                // Append concatenated text to formData
                formData.append('concatInput', this.text)

                this.uploadScript(formData)
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