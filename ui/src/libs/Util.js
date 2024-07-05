const Util = {
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
    }
}

export default Util;