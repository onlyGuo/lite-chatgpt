<script setup>
import {ref} from "vue";
import ScrollBar from "./ScrollBar.vue";
import Message from "./Message.vue";

const props = defineProps({
  chat: {
    type: Object,
    default: {
      id: '0',
      name: '',
      lastContent: '',
      avatar: '',
      time: '',
      desc: ''
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
    content: 'Hello, how can I help you?\n### title \n **bold** \n *italic* \n [link](https://www.baidu.com) \n```java \n' +
        'public static void main(String[] args){\n  System.out.println("Hello World");\n}\n``` ' +
        '\n ```function-call\nrun\nfinish\n```\n hahaha \n > aaa \n > bbb \n > ccc \n > ddd \n > eee \n > fff \n > ggg \n > hhh \n > iii ',
    time: '2021-09-01 12:00:00',
    attachments: []
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
  <div class="message-list">
    <scroll-bar>
      <div class="content">
        <message :message="message" v-for="(message, i) in messages" :key="i"/>
      </div>
    </scroll-bar>
  </div>
  <div class="footer" :style="'height:' + footerHeight + 'px'">
    <div class="move-line" @mousedown="moveLineOnMouseEnter"></div>
    <div class="tools-bar">
      <div class="left-tools">
        <div class="icon-button"><img src="../assets/vue.svg" alt="emoji" /></div>
        <div class="icon-button"><img src="../assets/vue.svg" alt="image" /></div>
        <div class="icon-button"><img src="../assets/vue.svg" alt="file" /></div>
      </div>
      <div class="right-tools">
        <div class="icon-button"><img src="../assets/vue.svg" alt="setting" /></div>
      </div>
    </div>
    <div class="input-area"></div>
  </div>
</div>
</template>

<style scoped lang="less">
.chat-box{
  display: flex;
  flex-direction: column;
  height: 100%;

  .header{
    display: flex;
    padding: 7px 10px;
    border-bottom: 1px solid #f0f0f0;
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


      .left-tools{
        display: flex;
        .icon-button{
          width: 30px;
          height: 30px;
          margin-right: 10px;
          img{
            width: 100%;
            height: 100%;
          }
        }
      }
      .right-tools{
        display: flex;
        .icon-button{
          width: 30px;
          height: 30px;
          margin-right: 10px;
          img{
            width: 100%;
            height: 100%;
          }
        }
      }
    }
    .input-area{
      flex: 1;
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
</style>