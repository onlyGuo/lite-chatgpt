import axios from "axios";

axios.defaults.timeout = 10000
axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8'

axios.interceptors.request.use(
    config => {
        let token = localStorage.getItem('token');
        if (token){
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    }
)
axios.interceptors.response.use(
    (response) => {
        if (response.status === 200) {
            return Promise.resolve(response)
        } else {
            return Promise.reject(response)
        }
    },
    // 服务器状态码不是200的情况
    error => {
        if (error.response){
            if (error.response.status) {
                if (error.response.status === 401) {
                } else if (error.response.status === 400) {
                } else if (error.response.status === 403){
                    console.error('您无权访问该内容')
                } else {
                    console.error('网络错误，请稍后再试')
                }
                return Promise.reject(error)
            }
        }
    }
)

export default axios
