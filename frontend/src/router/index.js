import { createRouter, createWebHistory } from 'vue-router'
import AdminLogin from '../views/AdminLogin.vue'
import AdminDashboard from '../views/AdminDashboard.vue'

const routes = [
    {
        path: '/admin/login',
        name: 'AdminLogin',
        component: AdminLogin
    },
    {
        path: '/admin/dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        beforeEnter: (to, from, next) => {
            const token = localStorage.getItem('admin_token')
            if (!token) {
                next('/admin/login')
            } else {
                next()
            }
        }
    },
    {
        path: '/admin',
        redirect: '/admin/login'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
