import {ref} from "vue";

const Util = {
    alertMessages: ref([]),
    getTimeSimpleString: (time, defaultValue) => {
        if (!defaultValue){
            defaultValue = '';
        }
        if (!time) {
            return defaultValue;
        }
        const date = new Date(time)
        // 刚刚、n分钟前、n小时前、n天前、n月前、n年前
        const now = new Date()
        const diff = now - date
        const diffSeconds = diff / 1000
        const diffMinutes = diffSeconds / 60
        const diffHours = diffMinutes / 60
        const diffDays = diffHours / 24
        const diffMonths = diffDays / 30
        const diffYears = diffMonths / 12
        if (diffSeconds < 60) {
            return '刚刚'
        } else if (diffMinutes < 60) {
            return `${Math.floor(diffMinutes)}分钟前`
        } else if (diffHours < 24) {
            return `${Math.floor(diffHours)}小时前`
        } else if (diffDays < 30) {
            return `${Math.floor(diffDays)}天前`
        } else if (diffMonths < 12) {
            return `${Math.floor(diffMonths)}月前`
        } else {
            return `${Math.floor(diffYears)}年前`
        }
    },

    getLocalStorage: (key, defaultValue) => {
        const str = localStorage.getItem(key);
        if(!str){
            localStorage.setItem(key, JSON.stringify(defaultValue));
            return defaultValue;
        }else{
            try {
                return JSON.parse(str);
            }catch (e) {
                localStorage.setItem(key, JSON.stringify(defaultValue));
                return defaultValue;
            }
        }
    },
    setLocalStorage: (key, value) => {
        localStorage.setItem(key, JSON.stringify(value));
    },
    alert: (title, content) => {
        const id = new Date().getTime();
        Util.alertMessages.value.push({title, content, id: id});
        return id;
    },
    closeAlert: (id) => {
        const index = Util.alertMessages.value.findIndex(item => item.id === id);
        if(index >= 0){
            Util.alertMessages.value.splice(index, 1);
        }
    }
}

export default Util;