<script setup>
import {nextTick, onMounted, ref, watch} from "vue";
import ScrollBar from "./ScrollBar.vue";
import Message from "./Message.vue";
import Util from "../libs/Util.js";
import SliderBar from "./SliderBar.vue";
import api from "../libs/api.js";

const props = defineProps({
  chat: {
    type: Object,
    default: {
      id: '0',
      name: '',
      lastContent: '',
      avatar: '',
      time: '',
      desc: '',
      inner: true,
      systemPrompt: '',
      model: 'gpt-3.5-turbo',
      contentRows: 3,
      plugin: []
    }
  }
})

const footerHeight = ref(200);
let moveStartIndex = 0;
let lastFooterHeight = 0;
let maxFooterHeight = 400;
const moveLineOnMouseEnter = (e) => {
  lastFooterHeight = footerHeight.value;
  moveStartIndex = e.clientY;
  // 取屏幕最大高度 - 200
  maxFooterHeight = window.innerHeight - 200;
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}
const onMouseMove = (e) => {
  const temp = lastFooterHeight + (moveStartIndex - e.clientY);
  if(temp < 100) {
    footerHeight.value = 100;
  } else if(temp > maxFooterHeight) {
    footerHeight.value = maxFooterHeight;
  } else {
    footerHeight.value = temp;
  }
}
const onMouseUp = (e) => {
  document.removeEventListener('mousemove', onMouseMove);
  document.removeEventListener('mouseup', onMouseUp);
}

const messages = ref([
  {
    role: 'assistant',
    content: 'Hello, how can I help you?\n```python\nprint("Hello, how can I help you?")\n```\n[[gpt-fun-call(DALL·E)\nrunning\ndone\n]]\n三角形的公式有很多种，取决于你想计算什么。以下是一些常见的三角形公式：\n' +
        '\n' +
        '#### 1.1. 底和高已知的三角形面积公式\n' +
        '\n' +
        '对于已知底边长度 \\(b\\) 和高 \\(h\\) 的三角形，面积 \\(A\\) 可以通过以下公式计算：\n' +
        '\n' +
        '\\[ A = \\frac{1}{2} \\times b \\times h \\]\n' +
        '\n' +
        '#### 1.2. 三边已知的三角形面积公式（海伦公式）\n' +
        '\n' +
        '对于已知三边长度 \\(a\\)、\\(b\\) 和 \\(c\\) 的三角形，面积 \\(A\\) 可以通过海伦公式计算：\n' +
        '\n' +
        '\\[ s = \\frac{a + b + c}{2} \\]\n' +
        '\\[ A = \\sqrt{s(s - a)(s - b)(s - c)} \\]\n' +
        '\n' +
        '其中，\\(s\\) 是半周长。\n',

    time: '2021-09-01 12:00:00',
    attachments: [
      {
        type: 'image',
        url: 'https://www.baidu.com/img/flexible/logo/pc/result.png'
      }
    ]
  },
  {
    role: 'user',
    content: 'Hello, how can I help you?',
    time: '2021-09-01 12:00:00',
    attachments: [
      {
        type: 'image',
        url: 'https://www.baidu.com/img/flexible/logo/pc/result.png'
      }
    ]
  }
]);

const plugins = ref([])
plugins.value = [];
api.get('api/v1/plugin').then(response => {
  response.data.forEach(plugin => {
    plugin.enable = props.chat.plugin.includes(plugin.pluginName);
    console.log(plugin)
    plugins.value.push(plugin);
  })
});

messages.value = Util.getLocalStorage("message_list_" + props.chat.id, [])
const openSetting = ref(false);

watch(props.chat, (newVal, oldVal) => {
  const list = Util.getLocalStorage("default_chat_list", []);
  const index = list.findIndex(item => item.id === newVal.id);
  if(index !== -1) {
    list[index] = newVal;
  } else {
    list.push(newVal);
  }
  Util.setLocalStorage("default_chat_list", list)
}, {deep: true})


const onInput = (e) => {
  if(e.isComposing){
    return;
  }
  // 要考虑MacOS下的换行符
  if(e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    const content = e.target.value;
    if (content.trim() === '') {
      return;
    }
    messages.value.push({
      role: 'user',
      content: content,
      time: new Date().toLocaleString(),
      attachments: []
    });
    e.target.value = '';
    Util.setLocalStorage("message_list_" + props.chat.id, messages.value)
    toBottom();

    fetch('/api/v1/gpt/message' , {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json;charset=utf-8',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      },
      body: JSON.stringify({
        model: props.chat.model,
        chatId: props.chat.id,
        messages: messages.value.slice(props.chat.contentRows * -1),
        plugins: plugins.value.filter(plugin => plugin.enable).map(plugin => plugin.pluginName),
      })
    }).then(response => {
      if (response.status === 401){
        const reply = ref({
          role: 'assistant',
          content: '这是一个新的浏览器，为了防止你是爬虫，在开始之前请先验证你是否是人类。 验证之后，我们会向你的浏览器写入信息。如果你更换了浏览器，你需要重新验证。并且更换浏览器后，聊天记录会消失。\n[我是人类](/#/user)',
          time: new Date().toLocaleString(),
          attachments: []
        })
        messages.value.push(reply.value);
        Util.setLocalStorage("message_list_" + props.chat.id, messages.value)
        return;
      }
      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');
      const reply = ref({
        role: 'assistant',
        content: '',
        time: new Date().toLocaleString(),
        attachments: []
      })
      messages.value.push(reply.value);
      return reader.read().then(function processText({ done, value }) {
        if (done) {
          Util.setLocalStorage("message_list_" + props.chat.id, messages.value)
          return;
        }
        let allLine = decoder.decode(value);
        let lines = [allLine]
        if (allLine.concat('\n')){
          lines = allLine.split('\n');
        }
        for(let i in lines){
          let line = lines[i].trim();
          if (line === ''){
            continue;
          }
          if (line.startsWith("{") && line.endsWith("}")){
            reply.value.content += '\n````\n' + line +"\n````\n";
            toBottom();
            return reader.read().then(processText);
          }
          if (line === ''){
            toBottom();
            return reader.read().then(processText);
          }
          if (line === '[DONE]'){
            toBottom();
            return reader.read().then(processText);
          }
          if (line.startsWith("data:")){
            line = line.substring(5);
          }
          try{
            const json = JSON.parse(line);
            reply.value.content += json.toString();
          }catch (e){
            return;
          }
        }
        toBottom();
        Util.setLocalStorage("message_list_" + props.chat.id, messages.value)
        return reader.read().then(processText);
      });

    }).catch(error => {
      console.log(error)
    });
  }
}
const messagesScroll = ref(null);
const toBottom = () => {
  nextTick(() => {
    messagesScroll.value.toBottom();
  })
}
onMounted(() => {
  toBottom();
})

watch(plugins, () => {
  props.chat.plugin = plugins.value.filter(plugin => plugin.enable).map(plugin => plugin.pluginName);
}, {deep: true});
const messageInput = ref(null);
const emojiList = ref([
    ['😊','😂','🤣','😘','😍','😒','🥰','😘'],
    ['😋','😁','😎','😉','😆','😅','😮','😥'],
    ['😉','😎','😢','😜','🥲','😙','😗','😣'],
    ['😏','🙄','😶‍🌫️','🤐','😯','😪','😫','🥱'],
    ['😴','😒','🤤','😌','😭','😬','😨','😱'],
    ['🥵','🤬','🤪','😡','😵','🤢','😈','🤓'],
    ['🧐','💕','❤️','💖','🙈','🙉','🙊','🐵'],
    ['✌️','🤞','👍','👌','🙌','💪','👂','🫰'],
    ['🐶','👇','👆','🫵','🫳','🫴','👏','👊'],
]);
const showSelectEmj = ref(false);
const inputContent = ref('');
const selectEmj = (emj) => {
  inputContent.value += emj;
  showSelectEmj.value = false;
  // 获得焦点
  messageInput.value.focus();
}
</script>

<template>
<div class="chat-box">
  <div class="header">
    <div class="left">
      <div class="avatar">
        <img :src="chat.avatar" alt="avatar" />
      </div>
    </div>
    <div class="right">
      <div class="name">{{chat.name}}</div>
      <div class="desc">
        <div class="model-name">gpt-3.5-turbo</div>
        <div class="model-desc">{{chat.desc}}</div>
      </div>
    </div>
  </div>
  <div class="main">
    <div class="left-content">
      <div class="message-list">
        <scroll-bar ref="messagesScroll">
          <div class="content">
            <div style="height: 51px"></div>
            <message :message="message" v-for="(message, i) in messages" :key="i" :gpt-avatar="chat.avatar" :on-update="toBottom"/>
          </div>
        </scroll-bar>
      </div>
      <div class="footer" :style="'height:' + footerHeight + 'px'">
        <div class="move-line" @mousedown="moveLineOnMouseEnter"></div>
        <div class="tools-bar">
          <div class="left-tools">
            <div class="icon-button" @click="showSelectEmj = !showSelectEmj"><img src="../assets/emoj.svg" alt="emoji" /></div>
            <div class="emj-box" v-if="showSelectEmj">
              <scroll-bar>
                <div class="emj-line" v-for="rows in emojiList">
                  <div class="emj-item" v-for="i in rows" @click="selectEmj(i)">{{i}}</div>
                </div>
              </scroll-bar>
            </div>
            <div class="icon-button"><img src="../assets/image.svg" alt="image" /></div>
            <div class="icon-button"><img src="../assets/attachment.svg" alt="file" /></div>
          </div>
          <div class="right-tools">
            <div class="icon-button" @click="openSetting = !openSetting"><img src="../assets/chat_setting.svg" alt="setting" /></div>
          </div>
        </div>
        <div class="input-area">
          <textarea class="input" ref="messageInput" placeholder="Enter text, press (Enter) to send, press (Shift+Enter) to wrap lines." @keydown="onInput" v-model="inputContent"></textarea>
        </div>
      </div>
    </div>
    <div class="right-chat-setting" :class="{open: openSetting}">
      <scroll-bar>
        <div style="height: 51px"></div>
        <div class="setting-title">Chat setting</div>
        <div class="block">
          <div class="block-title">Conversation</div>
          <div class="block-content">
            <div class="setting-item">
              <div class="title">Avatar</div>
              <div class="content">
                <div class="item">
                  <img :src="props.chat.avatar" class="image-input" />
                </div>
              </div>
            </div>
            <div class="setting-item">
              <div class="title">Name</div>
              <div class="content">
                <div class="item">
                  <input type="text" v-model="props.chat.name" :disabled="props.chat.inner"/>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="block">
          <div class="block-title">Assistant</div>
          <div class="block-content">

            <div class="setting-item">
              <div class="title">System prompt</div>
              <div class="content">
                <div class="item">
                  <textarea placeholder="Type a system prompt word" :disabled="props.chat.inner"></textarea>
                </div>
              </div>
            </div>

            <div class="setting-item">
              <div class="title">Model</div>
              <div class="content">
                <div class="item">
                  <select :disabled="props.chat.inner">
                    <option value="gpt-3.5-turbo">gpt-3.5-turbo</option>
                    <option value="gpt-4o">gpt-4o</option>
                    <option value="claude-3">claude-3</option>
                    <option value="claude-3.5">claude-3.5</option>
                    <option value="gemini-1.5">gemini-1.5</option>
                  </select>
                </div>
              </div>
            </div>

            <div class="setting-item">
              <div class="title">Content Rows</div>
              <div class="content">
                <div class="item">
                  <slider-bar v-model:value="props.chat.contentRows" :min="1" :max="10" :step="1" />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="block">
          <div class="block-title">Plugins</div>
          <div class="block-content">
            <div class="plugin-list">
              <div class="plugin-item" v-for="(plugin, i) in plugins" :key="i">
                <div class="left-icon">
                  <img :src="plugin.pluginIcon">
                </div>
                <div class="center-content">
                  <div class="title">{{plugin.pluginName}}</div>
                  <div class="desc">{{plugin.pluginDescription}}</div>
                </div>
                <div class="right-switch">
                  <input type="checkbox" v-model="plugin.enable"/>
                </div>
              </div>
            </div>
          </div>
        </div>
      </scroll-bar>
    </div>
  </div>
</div>
</template>

<style scoped lang="less">
.chat-box{
  display: flex;
  flex-direction: column;
  height: 100%;

  .header{
    position: fixed;
    z-index: 10;
    display: flex;
    padding: 7px 10px;
    border-bottom: 1px solid #f0f0f0;
    background-color: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(20px);
    width: 100%;
    .left{
      .avatar{
        width: 35px;
        height: 35px;
        border-radius: 50%;
        overflow: hidden;
        img{
          width: 100%;
          height: 100%;
        }
      }
    }
    .right{
      display: flex;
      flex-direction: column;
      justify-content: center;
      margin-left: 10px;
      .name{
        font-size: 16px;
        font-weight: bold;
      }
      .desc{
        display: flex;
        .model-name{
          font-size: 12px;
          color: #999;
          margin-right: 10px;
          padding: 2px 5px;
          background: #f0e5ff;
        }
        .model-desc{
          font-size: 12px;
          color: #999;
          line-height: 14px;
        }
      }
    }
  }
  .main{
    display: flex;
    flex: 1;
    .left-content{
      flex: 1;
      display: flex;
      flex-direction: column;
      .message-list{
        flex: 1;
        .content{
          padding: 10px;
        }
      }
      .footer{
        display: flex;
        flex-direction: column;
        .move-line{
          width: 100%;
          height: 3px;
          border-bottom: 1px solid #f0f0f0;
          background-color: transparent;
          transition: background-color 0.3s;
          cursor: n-resize;
          &:hover{
            background-color: #a578ff;
          }
        }
        .tools-bar{
          display: flex;
          user-select: none;
          justify-content: space-between;
          border-bottom: 1px solid #f0f0f0;
          padding: 5px;


          .left-tools{
            display: flex;
            .icon-button{
              width: 20px;
              height: 20px;
              margin-right: 10px;
              cursor: pointer;
              &:hover{
                opacity: .5;
              }
              img{
                width: 100%;
                height: 100%;
              }
            }
            .emj-box{
              position: absolute;
              margin-top: -160px;
              width: 250px;
              height: 150px;
              background-color: white;
              border: 1px solid #f0f0f0;
              border-radius: 5px;
              overflow: auto;
              z-index: 10;
              .emj-line{
                display: flex;
                justify-content: space-between;

                .emj-item{
                  width: 30px;
                  height: 30px;
                  font-size: 20px;
                  cursor: pointer;
                  &:hover{
                    background-color: #f0f0f0;
                  }
                }
              }

            }
          }
          .right-tools{
            display: flex;
            .icon-button{
              width: 20px;
              height: 20px;
              margin-right: 10px;
              cursor: pointer;
              &:hover{
                opacity: .5;
              }
              img{
                width: 100%;
                height: 100%;
              }
            }
          }
        }
        .input-area{
          flex: 1;
          .input{
            width: calc(100% - 20px);
            height: calc(100% - 25px);
            padding: 10px;
            border: none;
            outline: none;
            resize: none;
            overflow: auto;
            white-space: pre-wrap;
            font-size: 15px;
            font-family: 'Arial', sans-serif;
            // 占位文字颜色
            &::placeholder {
              color: #aaa;
            }
          }
        }
        button{
          width: 60px;
          height: 30px;
          background-color: #a578ff;
          color: #fff;
          border: none;
          border-radius: 5px;
        }
      }
    }
    .right-chat-setting{
      width: 0;
      height: 100%;
      transition: width 0.3s;
      border-left: 1px solid #f0f0f0;
      &.open{
        width: 300px;
      }

      .setting-title{
        font-size: 16px;
        font-weight: bold;
        text-align: center;
        padding: 10px;
      }

      .block {
        .block-title{
          font-size: 14px;
          font-weight: bold;
          margin-bottom: 10px;
          border-top: 1px solid #f0f0f0;
          border-bottom: 1px solid #f0f0f0;
          padding: 5px 10px;
          background-color: #fcfcfc;
        }
        .block-content {
          padding: 10px;
          .setting-item {
            margin-bottom: 10px;

            .title {
              font-size: 14px;
              font-weight: bold;
              margin-bottom: 5px;
            }

            .content {
              .item {
                width: calc(100% - 10px);
                input, textarea, select {
                  width: 100%;
                  padding: 5px;
                  border: 1px solid #f0f0f0;
                  border-radius: 5px;
                  outline: none;
                }
                select{
                  cursor: pointer;
                  width: calc(100% + 10px)!important;
                }
                textarea {
                  height: 100px;
                  resize: none;
                }

                .image-input {
                  width: 100px;
                  height: 100px;
                  border: 1px solid #f0f0f0;
                  border-radius: 5px;
                }
              }
            }
          }

          .plugin-list{
            .plugin-item {
              display: flex;
              margin-bottom: 10px;

              .left-icon {
                width: 40px;
                height: 40px;
                margin-right: 10px;
                border: 1px solid #f0f0f0;
                border-radius: 5px;
                overflow: hidden;
                background-color: #f0f0f0;

                img {
                  width: 100%;
                  height: 100%;
                }
              }

              .center-content {
                flex: 1;

                .title {
                  font-size: 14px;
                  font-weight: bold;
                }

                .desc {
                  font-size: 12px;
                  color: #999;
                }
              }

              .right-switch {
                input[type="checkbox"] {
                  width: 40px;
                  height: 20px;
                  -webkit-appearance: none;
                  background-color: #c0c0c0;
                  border-radius: 20px;
                  position: relative;
                  transition: background-color .3s;
                  cursor: pointer;

                  &:before {
                    content: "";
                    position: absolute;
                    width: 20px;
                    height: 20px;
                    border-radius: 50%;
                    background-color: #fff;
                    left: 0;
                    transition: left .3s;
                  }

                  &:checked {
                    background-color: #a578ff;

                    &:before {
                      left: 20px;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

}
</style>