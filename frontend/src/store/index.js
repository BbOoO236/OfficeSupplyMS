import Vue from 'vue'
import Vuex from 'vuex'
import Cookies from 'js-cookie'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: Cookies.get('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    sidebar: {
      opened: Cookies.get('sidebarStatus') ? !!+Cookies.get('sidebarStatus') : true
    }
  },
  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
      Cookies.set('token', token)
    },
    SET_USER_INFO: (state, userInfo) => {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    REMOVE_TOKEN: (state) => {
      state.token = ''
      Cookies.remove('token')
    },
    REMOVE_USER_INFO: (state) => {
      state.userInfo = {}
      localStorage.removeItem('userInfo')
    },
    TOGGLE_SIDEBAR: (state) => {
      state.sidebar.opened = !state.sidebar.opened
      if (state.sidebar.opened) {
        Cookies.set('sidebarStatus', 1)
      } else {
        Cookies.set('sidebarStatus', 0)
      }
    }
  },
  actions: {
    login({ commit }, userInfo) {
      return new Promise((resolve, reject) => {
        // 这里调用登录API
        // 模拟登录
        const token = 'mock-token-' + Date.now()
        commit('SET_TOKEN', token)
        commit('SET_USER_INFO', {
          userId: 1,
          username: userInfo.username,
          realName: '管理员',
          role: 'ADMIN',
          department: 'IT部'
        })
        resolve()
      })
    },
    logout({ commit }) {
      return new Promise((resolve) => {
        commit('REMOVE_TOKEN')
        commit('REMOVE_USER_INFO')
        resolve()
      })
    }
  },
  getters: {
    token: state => state.token,
    userInfo: state => state.userInfo,
    role: state => state.userInfo.role || '',
    username: state => state.userInfo.username || '',
    realName: state => state.userInfo.realName || '',
    department: state => state.userInfo.department || '',
    sidebar: state => state.sidebar
  }
})