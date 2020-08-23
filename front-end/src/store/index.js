import Vue from 'vue'
import Vuex from 'vuex'
import forceDirectedGraph from './modules/forceDirectedGraph'

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
        forceDirectedGraph
    }
  })