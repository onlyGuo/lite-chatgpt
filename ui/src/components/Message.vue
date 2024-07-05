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
  }
})
</script>

<template>
  <div class="message">
    <div v-if="message.role === 'assistant'" class="assistant-message">
      <div class="avatar">
        <img src="../assets/vue.svg" alt="avatar" />
      </div>
      <div class="content">
        <MdPreview :model-value="message.content" class="markdown-body" />
      </div>
    </div>
    <div v-else class="user-message">
      <div class="content">
        <MdPreview :model-value="message.content" class="markdown-body" />
      </div>
      <div class="avatar">
        <img src="../assets/vue.svg" alt="avatar" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.message{
  .content{
    box-shadow: 2px 2px 10px -3px rgba(0,0,0,0.4);

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
      background-color: white;
      padding: 10px;
      border-radius: 5px;
      margin-left: 10px;
      max-width: 70%;
      word-break: break-all;
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