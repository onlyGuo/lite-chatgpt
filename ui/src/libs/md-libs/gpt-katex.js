export default (md, options) => {

    // 正则表达式匹配 \[ ... \] 包裹的公式
    const inlineMath = /\$\$(.+?)\$\$/g;
    const blockMath = /\\\[(.+?)\\\]/g;


    // 将 \[ ... \] 转换为 $$ ... $$
    function replaceMath(text) {
        return text.replace(blockMath, (match, p1) => `$$${p1}$$`);
    }

    md.core.ruler.before('normalize',"custom_katex", (state, silent) => {
        state.src = replaceMath(state.src);
    })
}