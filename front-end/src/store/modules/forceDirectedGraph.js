const state = {
    nodes: [{
            id: 0,
            name: 'salesgroup_member',
            _color: 'blue'
        },
        {
            id: 1,
            name: 'merchant',
            _color: 'orange'
        },
        {
            id: 2,
            name: 'salesgroup_account',
            _color: 'red'
        },
        {
            id: 3,
            name: 'item',
            _color: 'green'
        },
        {
            id: 4,
            name: 'cleaned_item_view',
            _color: 'black'
        },
        {
            id: 5,
            name: 'salesforce_item_relationship',
            _color: 'grey'
        },
        {
            id: 6,
            name: 'item_with_client_details',
            _color: 'pink'
        },
        {
            id: 7,
            name: 'cleaned_address',
            _color: 'purple'
        },
        {
            id: 8,
            name: 'client_prescreened',
            _color: 'yellow'
        }
    ],
    links: [
        {
            sid: 0,
            tid: 1,
            _color: 'red'
          },
          {
            sid: 2,
            tid: 1,
            _color: 'red'
          },
          {
            sid: 2,
            tid: 6,
            _color: 'red'
          },
          {
            sid: 2,
            tid: 8,
            _color: 'red'
          },
          {
            sid: 3,
            tid: 4,
            _color: 'red'
          },
          {
            sid: 4,
            tid: 1,
            _color: 'red'
          },
          {
            sid: 4,
            tid: 6,
            _color: 'red'
          },
          {
            sid: 4,
            tid: 8,
            _color: 'red'
          },
          {
            sid: 5,
            tid: 6,
            _color: 'red'
          },
          {
            sid: 5,
            tid: 8,
            _color: 'red'
          },
          {
            sid: 5,
            tid: 1,
            _color: 'red'
          },
          {
            sid: 7,
            tid: 1,
            _color: 'red'
          },
          {
            sid: 7,
            tid: 8,
            _color: 'red'
          }
    ]
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

const mutations = {}

export default {
    state: state,
    getters: getters,
    actions: actions,
    mutations: mutations
}