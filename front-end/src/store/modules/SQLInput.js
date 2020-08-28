const state = {
    isUploaded: false
}

const getters = {
    getisUploaded(state){
        return state.isUploaded
    }
}

const actions = {
    // Change state of isUplaoded, just a test for now so we can see component rendering
    uploadScript(context){
        context.commit('uploadScript')
    }
}

const mutations = {
    uploadScript(state){
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