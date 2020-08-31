const state = {
    tables: [],
    nodes: [],
    links: []
}

const getters = {
    allNodes: (state) => {
        return state.nodes
    },
    allLinks: (state) => {
        return state.links
    }
}

const actions = {}

const mutations = {
    SET_TABLES(state, tables){
      state.tables = tables
      state.tables.forEach(element => {
        // Creating Nodes
        state.nodes.push({
          id: parseInt(element.id),
          name: element.name
        })
        //creating links
         element.children_id.forEach(children_id => {
            if(children_id == "")
            {
              return
            }
           state.links.push({
            sid: parseInt(element.id),
            tid: parseInt(children_id),
            _color: 'red'
           })
         })
      });
    }
}

export default {
    state: state,
    getters: getters,
    actions: actions,
    mutations: mutations
}