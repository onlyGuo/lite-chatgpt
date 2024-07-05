import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'Index',
        component: () => import('../views/ChatView.vue'),
    },
    {
        path: '/chat',
        name: 'Chat',
        component: () => import('../views/ChatView.vue'),
    },
    {
        path: '/user',
        name: 'User',
        component: () => import('../views/UserView.vue'),
    },
    {
        path: '/bbs',
        name: 'BBS',
        component: () => import('../views/BBSView.vue'),
    },
    {
        path: '/setting',
        name: 'Setting',
        component: () => import('../views/SettingView.vue'),
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router