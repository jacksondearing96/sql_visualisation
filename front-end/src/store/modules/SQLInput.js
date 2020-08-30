import axios from 'axios'

const state = {
    isUploaded: false
}

const getters = {
    getisUploaded(state) {
        return state.isUploaded
    }
}

const actions = {
    // POST request
    uploadScript(context, formData) {
        axios.post('http://127.0.0.1:5000/uploader', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }).then(response => {
            console.log(response)
            context.commit('uploadScript')
        }).catch(error => {
            console.log('POST request upload script error: ' + error)
        })
    }
}

const mutations = {
    uploadScript(state) {
        // Mutate isUploaded
        state.isUploaded = true
    }
}

export default {
    state: state,
    getters: getters,
    actions: actions,
    mutations: mutations
}