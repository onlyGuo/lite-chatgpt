<script setup>
import 'md-editor-v3/lib/style.css';
import {MdPreview} from "md-editor-v3";
const props = defineProps({
  message: {
    type: Object,
    default: {
      role: 'assistant',
      content: '',
      time: '',
      attachments: []
    },
  },
  gptAvatar: {
    type: String,
    default: ''
  },
  onUpdate: {
    type: Function,
    default: () => {}
  }
})
</script>

<template>
  <div class="message">
    <div v-if="message.role === 'assistant'" class="assistant-message">
      <div class="avatar">
        <img :src="gptAvatar" alt="avatar" />
      </div>
      <div class="content">
        <MdPreview :model-value="message.content" class="markdown-body" :codeFoldable="false" :on-html-changed="onUpdate"/>
      </div>
    </div>
    <div v-else class="user-message">
      <div class="content">
        <MdPreview :model-value="message.content" class="markdown-body" :codeFoldable="false" :on-html-changed="onUpdate"/>
      </div>
      <div class="avatar">
        <img src="../assets/avatar.svg" alt="avatar" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.message{
  .content{
    box-shadow: 1px 1px 7px -2px rgba(0,0,0,0.2);

    .markdown-body{
      background-color: transparent;
    }
    /deep/.md-editor-preview-wrapper{
      padding: 0;
      p{
        margin: 0;
      }
    }
  }
  .assistant-message{
    display: flex;
    margin-bottom: 10px;
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
    .content{
      background-color: #fcfcfc;
      padding: 10px;
      border-radius: 5px;
      margin-left: 10px;
      max-width: 70%;
      word-break: break-all;

      /* markdown */
      .markdown-body{
        /deep/.gpt-fun-call{
          background-color: rgba(0,0,0,.05);
          padding: 5px 7px;
          border-radius: 5px;
          margin: 5px 0;
          width: max-content;
          cursor: pointer;
          pointer-events: none;
          .fun-icon{
            margin-right: 5px;
            height: 20px;
            width: 20px;
            padding: 0;
            img{
              width: 100%;
              height: 100%;
              margin: 0;
              padding: 0;
              border: none;
            }
          }
          .gpt-fun-call-header{
            font-size: 14px;
            font-weight: bold;
            color: #35b378;
            display: flex;
          }
          .gpt-fun-call-status{
            margin-top: 5px;
            display: flex;
            font-size: 12px;
            color: #7a7a7a;
            .fun-icon{
              margin-right: 5px;
              margin-top: 2px;
              height: 15px;
              width: 15px;
              padding: 0;
              &.running{
                animation: rotate 2s linear infinite;
              }
            }
            @keyframes rotate {
              0% {
                transform: rotate(0deg);
              }
              100% {
                transform: rotate(360deg);
              }
            }
          }
        }
      }
    }
  }

  .user-message{
    display: flex;
    justify-content: flex-end;
    margin-bottom: 10px;
    .content{
      background-color: #a578ff;
      padding: 10px;
      border-radius: 5px;
      margin-right: 10px;
      max-width: 70%;
      word-break: break-all;
      color: #fff;
      /deep/.md-editor div.default-theme{
        color: white!important;
      }
    }
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
}
</style>