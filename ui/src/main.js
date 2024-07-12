import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from "./router/index.js";
import { config } from 'md-editor-v3';
import gptFunCall from "./libs/md-libs/gpt-fun-call.js";
import gptKatex from "./libs/md-libs/gpt-katex.js";

// 自定义markdown-it扩展
config({
    markdownItConfig(mdit) {
        mdit.use(gptFunCall)
        mdit.use(gptKatex)
    }
});


const app = createApp(App)
app.use(router)





app.mount('#app')
