import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// 必须在 Element UI 导入之前加载主题覆盖
import '@/styles/element-variables.scss'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import '@/styles/index.scss'

import * as echarts from 'echarts'
Vue.prototype.$echarts = echarts

Vue.config.productionTip = false

Vue.use(ElementUI)

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')