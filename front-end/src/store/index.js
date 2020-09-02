import Vue from 'vue'
import Vuex from 'vuex'
import forceDirectedGraph from './modules/forceDirectedGraph'
import SQLInput from './modules/SQLInput'

// Load Vuex
Vue.use(Vuex)

// Create store
export default new Vuex.Store({
    state: {
    },
    mutations: {
    },
    actions: {
    },
    modules: {
        forceDirectedGraph,
        SQLInput
    }
  })