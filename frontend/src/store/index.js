import Vue from 'vue'
import Vuex from 'vuex'
import Cookies from 'js-cookie'
import { login as authLogin } from '@/api/auth'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: Cookies.get('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    sidebar: {
      opened: Cookies.get('sidebarStatus') ? !!+Cookies.get('sidebarStatus') : true
    },
    activeModule: 'dashboard' // 当前激活的模块标识
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
    },
    SET_ACTIVE_MODULE: (state, module) => {
      state.activeModule = module
    }
  },
  actions: {
    login({ commit }, userInfo) {
      return new Promise((resolve, reject) => {
        authLogin(userInfo).then(response => {
          const { data } = response
          // data应该包含token和用户信息
          commit('SET_TOKEN', data.token)
          commit('SET_USER_INFO', {
            userId: data.userId,
            username: data.username,
            realName: data.realName || '',
            role: data.role,
            department: data.department || ''
          })
          resolve()
        }).catch(error => {
          reject(error)
        })
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
    sidebar: state => state.sidebar,
    activeModule: state => state.activeModule,
    // 模块列表可以从constants中导入，但这里直接返回空数组，由组件导入
    modules: () => []
  }
})