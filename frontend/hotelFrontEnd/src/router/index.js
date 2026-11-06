import { createRouter, createWebHistory } from 'vue-router'

//（路由实例，使用 history 模式）
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/components/home.vue')
    },
    {
      path: '/logIn',
      component: () => import('@/components/logIn.vue')
    },
    {
      path: '/register',
      component: () => import('@/components/register.vue')
    },
    {
      path: '/manageLog',
      component: () => import('@/components/manageLog.vue')
    },
    //（客户端页面路由）
    {
      path: '/client',
      component: () => import('@/components/client.vue'),
      redirect: '/client/info',
      children: [
        {
          path: 'info',
          component: () => import('@/views/info.vue')
        },
        {
          path: 'rooms',
          component: () => import('@/views/rooms.vue')
        },
        {
          path: 'orders',
          component: () => import('@/views/orders.vue')
        }
      ]
    },
    {
      path: '/room/:id/:startDate?/:endDate?',
      component: () => import('@/components/room.vue')
    },
    //（员工端页面路由）
    {
      path: '/employee',
      component: () => import('@/components/employee.vue'),
      redirect:'/employee/adminInfo',
      children:[
        {
          path:'adminInfo',
          component: () => import('@/views/adminInfo.vue')
        },
        {
          path:'roomOrder',
          component: () => import('@/views/roomOrder.vue')
        },
        {
          path:'selectOrder',
          component: () => import('@/views/selectOrder.vue')
        },
        {
          path:'roomAndCat',
          component: () => import('@/views/roomAndCat.vue')
        },
        {
          path:'allAccount',
          component: () => import('@/views/allAccount.vue')
        }
      ]
    },
  ]
})



export default router