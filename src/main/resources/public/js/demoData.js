
var demoGraph = { 
  nodes: [
  {
    id: '%(crm)s_task::',
    name: '%(crm)s_task',
    group: '%(crm)s_task',
    incoming: [],
    outgoing: [],
    type: 'TABLE'
  },
  {
    id: '%(crm)s_task::accountid',
    name: 'accountid',
    group: '%(crm)s_task',
    order: '0',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },
  {
    id: '%(crm)s_task::ownerid',
    name: 'ownerid',
    group: '%(crm)s_task',
    order: '1',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },
  {
    id: '%(crm)s_task::status',
    name: 'status',
    group: '%(crm)s_task',
    order: '2',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },
  {
    id: '%(crm)s_task::activitydate',
    name: 'activitydate',
    group: '%(crm)s_task',
    order: '3',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },

  {
    id: 'customer_insight::',
    name: 'customer_insight',
    group: 'customer_insight',
    incoming: [],
    outgoing: [],
    type: 'TABLE'
  },
  {
    id: 'customer_insight::acct_sf_id',
    name: 'acct_sf_id',
    group: 'customer_insight',
    order: '0',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },
  {
    id: 'customer_insight::user_sf_id',
    name: 'user_sf_id',
    group: 'customer_insight',
    order: '1',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },

  {
    id: 'note_count_by_agent::',
    name: 'note_count_by_agent',
    group: 'note_count_by_agent',
    incoming: [],
    outgoing: [],
    type: 'VIEW'
  },
  {
    id: 'note_count_by_agent::acct_sf_id',
    name: 'acct_sf_id',
    group: 'note_count_by_agent',
    order: '0',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },
  {
    id: 'note_count_by_agent::user_sf_id',
    name: 'user_sf_id',
    group: 'note_count_by_agent',
    order: '1',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },
  {
    id: 'note_count_by_agent::cnt',
    name: 'cnt',
    group: 'note_count_by_agent',
    order: '2',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  },
  {
    id: 'note_count_by_agent::*',
    name: '*',
    group: 'note_count_by_agent',
    order: '3',
    incoming: [],
    outgoing: [],
    type: 'COLUMN'
  }
],
links: [
  {
    source: '%(crm)s_task::ownerid',
    target: 'customer_insight::acct_sf_id',
    id: 'link0'
  },
  {
    source: 'customer_insight::acct_sf_id',
    target: 'note_count_by_agent::acct_sf_id',
    id: 'link1'
  },
  {
    source: 'customer_insight::user_sf_id',
    target: 'note_count_by_agent::user_sf_id',
    id: 'link2'
  },
  {
    source: 'customer_insight::',
    target: 'note_count_by_agent::*',
    id: 'link3'
  },
  {
    source: '%(crm)s_task::status',
    target: 'customer_insight::',
    id: 'link4'
  }
]};