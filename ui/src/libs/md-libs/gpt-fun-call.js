export default (md, options) => {
    // 添加一个规则，解析
    // \[\[gpt-fun-call(this is fun name)
    //  this is fun content
    // \]\]这样的语法 渲染成如下的html
    // <div class="gpt-fun-call">
    //     <div class="gpt-fun-call-header">this is fun name</div>
    //     <div class="gpt-fun-call-content">this is fun content</div>
    // </div>
    md.inline.ruler.before('emphasis',"gpt_fun_call", (state, silent) => {
        const start = state.pos;
        const max = state.posMax;

        // Check for opening delimiter
        if (state.src.charCodeAt(start) !== 0x5B /* [ */ ||
            state.src.charCodeAt(start + 1) !== 0x5B /* [ */) {
            return false;
        }

        // Find the closing delimiter
        const end = state.src.indexOf('\]\]', start);
        let content;
        if (end === -1) {
            // If no closing delimiter, take the rest of the content
            content = state.src.slice(start + 2).trim();
            state.pos = state.posMax; // Move the position to the end
        } else {
            // Extract the content within the delimiters
            content = state.src.slice(start + 2, end).trim();
            state.pos = end + 2;
        }

        const match = content.match(/^gpt-fun-call\(([^)]+)\)\s+([\s\S]*)$/);

        if (!match) {
            return false;
        }

        if (!silent) {
            const token = state.push('gpt_fun_call', '', 0);
            token.content = {
                name: match[1].trim(),
                content: match[2].trim()
            };
        }

        return true;
    })

    // 渲染规则
    md.renderer.rules['gpt_fun_call'] = function(tokens, idx) {
        const { name, content } = tokens[idx].content;
        // 默认时running
        let funContentHtml = `
            <div class="gpt-fun-call-status">
                <div class="gpt-fun-call-status-icon fun-icon running">
                    <img src="/icons/refresh.svg" alt="">
                </div>
                <div class="gpt-fun-call-status-text">running</div>
            </div>
        `;
        if (content.endsWith('done')) {
            funContentHtml = `
                <div class="gpt-fun-call-status">
                    <div class="gpt-fun-call-status-icon fun-icon">
                        <img src="/icons/finish.svg" alt="">
                    </div>
                    <div class="gpt-fun-call-status-text">done</div>
                </div>
            `;
        }
        return `
            <div class="gpt-fun-call">
                <div class="gpt-fun-call-header">
                    <div class="fun-icon">
                        <img src="/icons/plugin.svg" alt="">
                    </div>
                    ${md.utils.escapeHtml(name)}
                </div>
                <div class="gpt-fun-call-content">${funContentHtml}</div>
            </div>
        `;
    };

}